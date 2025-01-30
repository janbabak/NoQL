package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatHistoryItem;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Column;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructureDto;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructureDto.SchemaDto;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructureDto.TableDto;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.query.*;
import com.janbabak.noqlbackend.service.JwtService;
import com.janbabak.noqlbackend.service.chat.ChatService;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.database.DatabaseEntityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.janbabak.noqlbackend.service.utils.JsonUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(JwtService.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatabaseEntityService databaseServiceMock;

    @MockBean
    @SuppressWarnings("unused") // mock is used internally
    private QueryService queryService;

    @MockBean
    private ChatService chatService;

    private final String ROOT_URL = "/database";

    private final User testUser = User.builder()
            .id(UUID.fromString("af11c153-2948-4922-bca7-3e407a40da02"))
            .build();

    private final Database localPostgres = Database.builder()
            .id(UUID.randomUUID())
            .name("Local Postgres")
            .host("localhost")
            .port(5432)
            .database("database")
            .userName("user")
            .password("password")
            .chats(new ArrayList<>())
            .engine(DatabaseEngine.POSTGRES)
            .user(testUser)
            .build();

    @Test
    @DisplayName("Get all databases")
    @WithMockUser(roles = "USER")
    void testGetAllDatabases() throws Exception {
        // given
        Database localMysql = Database.builder()
                .id(UUID.randomUUID())
                .name("Local MySQL")
                .host("localhost")
                .port(3306)
                .database("database")
                .userName("user")
                .password("password")
                .chats(new ArrayList<>())
                .engine(DatabaseEngine.MYSQL)
                .user(testUser)
                .build();
        List<Database> databases = List.of(localPostgres, localMysql);

        when(databaseServiceMock.findAll(null)).thenReturn(databases);

        // then
        mockMvc.perform(get(ROOT_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(databases), true));
    }

    @Test
    @DisplayName("Get all databases by anonymous user")
    @WithAnonymousUser
    void testGetAllDatabasesByAnotherUser() throws Exception {
        mockMvc.perform(get(ROOT_URL))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get database by id")
    @WithMockUser(roles = "USER")
    void testGetDatabaseById() throws Exception {
        when(databaseServiceMock.findById(localPostgres.getId())).thenReturn(localPostgres);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}", localPostgres.getId()))
                .andDo(print())
                .andExpect(content().json(toJson(localPostgres), true))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get database by id not found")
    @WithMockUser(roles = "USER")
    void testGetDatabaseByIdNotFound() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        when(databaseServiceMock.findById(databaseId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}", databaseId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get database by id by anonymous user")
    @WithAnonymousUser
    void testGetDatabaseByIdByAnotherUser() throws Exception {
        mockMvc.perform(get(ROOT_URL + "/{databaseId}", localPostgres.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @DisplayName("Create database")
    @MethodSource("createDatabaseDataProvider")
    @WithMockUser(roles = "USER")
    void testCreateDatabase(String request, Database createdDatabase, String response, Boolean success)
            throws Exception {

        if (success) {
            when(databaseServiceMock.create(any())).thenReturn(createdDatabase);
        }

        // then
        mockMvc.perform(post(ROOT_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
                        .with(csrf()))
                .andDo(print())
                .andExpect(success ? status().isCreated() : status().isBadRequest())
                .andExpect(content().json(response, true));
    }

    /**
     * @return request, created database, response, and success
     */
    Object[][] createDatabaseDataProvider() {
        return new Object[][]{
                {
                        // language=JSON
                        """
                    {
                        "name":"Local Postgres",
                        "host":"127.0.0.1",
                        "port":5432,
                        "database":"database",
                        "userName":"user",
                        "password":"password",
                        "engine":"POSTGRES",
                        "userId": "af11c153-2948-4922-bca7-3e407a40da02"
                    }""",
                        Database.builder()
                                .id(UUID.fromString("6678fc72-1a55-4146-b74b-b3f5aac677df"))
                                .name("Local Postgres")
                                .host("127.0.0.1")
                                .port(5432)
                                .database("database")
                                .userName("user")
                                .password("password")
                                .engine(DatabaseEngine.POSTGRES)
                                .user(testUser)
                                .build(),
                        // language=JSON
                        """
                    {
                        "id": "6678fc72-1a55-4146-b74b-b3f5aac677df",
                        "name":"Local Postgres",
                        "host":"127.0.0.1",
                        "port":5432,
                        "database":"database",
                        "userName":"user",
                        "password":"password",
                        "engine":"POSTGRES",
                        "isSQL": true,
                        "userId": "af11c153-2948-4922-bca7-3e407a40da02"
                    }""",
                        true,
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"Local Postgres",
                        "host":"localhost",
                        "port":-100,
                        "database":"database",
                        "userName":"user",
                        "password":"password",
                        "engine":"POSTGRES",
                        "userId": "af11c153-2948-4922-bca7-3e407a40da02"
                    }""",
                        null,
                        // language=JSON
                        """
                    {
                        "port": "must be greater than or equal to 1"
                    }""",
                        false,
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"Local Postgres database for testing purposes",
                        "host":"localhost-home",
                        "port":5432,
                        "database": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        "userName":"user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user user user",
                        "password":"password_password_password_password_password_password_password_password_password _password_password_password_password_password_password_password_password_password_password password_password_password_password_password_password_password_password_password_password password_password_password_password_password_password_password_password_password_password password_password_password_password_password_password_password_password_password_password",
                        "engine":"POSTGRES",
                        "userId": "af11c153-2948-4922-bca7-3e407a40da02"
                    }""",
                        null,
                        // language=JSON
                        """
                    {
                         "name": "length must be between 1 and 32",
                         "database":"length must be between 1 and 253",
                         "userName":"length must be between 1 and 128",
                         "password":"length must be between 1 and 253"
                     }""",
                        false
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"",
                        "host":"",
                        "port":100,
                        "database":"",
                        "userName":"",
                        "password":"",
                        "engine":"POSTGRES"
                    }""",
                        null,
                        // language=JSON
                        """
                    {
                          "name": "must not be blank",
                          "host": "must not be blank",
                          "database": "must not be blank",
                          "userName": "must not be blank",
                          "password": "must not be blank",
                          "userId": "must not be null"
                      }""",
                        false
                }
        };
    }

    @Test
    @DisplayName("Create database by anonymous user")
    @WithAnonymousUser
    void testCreateDatabaseByAnotherUser() throws Exception {
        mockMvc.perform(post(ROOT_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @DisplayName("Update database")
    @MethodSource("updateDatabaseDataProvider")
    @WithMockUser(roles = "USER")
    void testUpdateDatabase(String request, Database updatedDatabase, String response, Boolean success)
            throws Exception {

        // given
        UUID databaseId = UUID.fromString("6678fc72-1a55-4146-b74b-b3f5aac677df");

        if (success) {
            when(databaseServiceMock.update(eq(databaseId), any())).thenReturn(updatedDatabase);
        }

        // then
        mockMvc.perform(put(ROOT_URL + "/{databaseId}", databaseId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
                        .with(csrf()))
                .andDo(print())
                .andExpect(success ? status().isOk() : status().isBadRequest())
                .andExpect(content().json(response, true));
    }

    /**
     * @return request, updated database, response, and success flag
     */
    Object[][] updateDatabaseDataProvider() {
        return new Object[][]{
                {
                        // language=JSON
                        """
                    {
                        "name":"Updated name"
                    }""",
                        Database.builder()
                                .id(UUID.fromString("6678fc72-1a55-4146-b74b-b3f5aac677df"))
                                .name("Updated name")
                                .host("localhost")
                                .port(5432)
                                .database("database")
                                .userName("user")
                                .password("password")
                                .engine(DatabaseEngine.POSTGRES)
                                .user(testUser)
                                .build(),
                        // language=JSON
                        """
                    {
                        "id": "6678fc72-1a55-4146-b74b-b3f5aac677df",
                        "name":"Updated name",
                        "host":"localhost",
                        "port":5432,
                        "database":"database",
                        "userName":"user",
                        "password":"password",
                        "engine":"POSTGRES",
                        "userId": "af11c153-2948-4922-bca7-3e407a40da02",
                        "isSQL": true
                    }""",
                        true,
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"Updated name",
                        "host":"127.0.0.1",
                        "port":5555,
                        "database":"Updated database",
                        "userName":"Updated user",
                        "password":"Updated password",
                        "engine":"MYSQL"
                    }""",
                        Database.builder()
                                .id(UUID.fromString("6678fc72-1a55-4146-b74b-b3f5aac677df"))
                                .name("Updated name")
                                .host("127.0.0.1")
                                .port(5555)
                                .database("Updated database")
                                .userName("Updated user")
                                .password("Updated password")
                                .engine(DatabaseEngine.MYSQL)
                                .user(testUser)
                                .build(),
                        // language=JSON
                        """
                    {
                        "id": "6678fc72-1a55-4146-b74b-b3f5aac677df",
                        "name":"Updated name",
                        "host":"127.0.0.1",
                        "port":5555,
                        "database":"Updated database",
                        "userName":"Updated user",
                        "password":"Updated password",
                        "engine":"MYSQL",
                        "userId": "af11c153-2948-4922-bca7-3e407a40da02",
                        "isSQL": true
                    }""",
                        true,
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"Local Postgres",
                        "host":"localhost",
                        "port":-100,
                        "database":"database",
                        "userName":"user",
                        "password":"password",
                        "engine":"POSTGRES"
                    }""",
                        null,
                        // language=JSON
                        """
                    {
                        "port": "must be greater than or equal to 1"
                    }""",
                        false,
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"Local Postgres database for testing purposes",
                        "host":"localhost-invalid",
                        "port":5432,
                        "database": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        "userName":"user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user user user",
                        "password":"password_password_password_password_password_password_password_password_password _password_password_password_password_password_password_password_password_password_password password_password_password_password_password_password_password_password_password_password password_password_password_password_password_password_password_password_password_password password_password_password_password_password_password_password_password_password_password",
                        "engine":"POSTGRES"
                    }""",
                        null,
                        // language=JSON
                        """
                    {
                         "name": "length must be between 1 and 32",
                         "database":"length must be between 1 and 253",
                         "userName":"length must be between 1 and 128",
                         "password":"length must be between 1 and 253"
                     }""",
                        false
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"",
                        "host":"",
                        "port":100,
                        "database":"",
                        "userName":"",
                        "password":"",
                        "engine":"POSTGRES"
                    }""",
                        null,
                        // language=JSON
                        """
                    {
                        "name": "length must be between 1 and 32",
                        "host": "length must be between 1 and 253",
                        "database": "length must be between 1 and 253",
                        "userName": "length must be between 1 and 128",
                        "password": "length must be between 1 and 253"
                    }""",
                        false
                }
        };
    }

    @Test
    @DisplayName("Update database by anonymous user")
    @WithAnonymousUser
    void testUpdateDatabaseByAnotherUser() throws Exception {
        mockMvc.perform(put(ROOT_URL + "/{databaseId}", localPostgres.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Delete database")
    @WithMockUser(roles = "USER")
    void testDeleteDatabaseById() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        // then
        mockMvc.perform(delete(ROOT_URL + "/{databaseId}", databaseId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete database by anonymous user")
    @WithAnonymousUser
    void testDeleteDatabaseByIdByAnotherUser() throws Exception {
        mockMvc.perform(delete(ROOT_URL + "/{databaseId}", localPostgres.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Execute chat")
    @WithMockUser(roles = "USER")
    void testQueryChat() throws Exception {

        // given
        Integer pageSize = 2;
        UUID databaseId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        QueryRequest request = new QueryRequest("find all users older than 25", "gpt-4o");
        ChatResponse response = ChatResponse.builder()
                .dbQuery("SELECT * FROM users WHERE age > 25")
                .nlQuery("find all users older than 25")
                .timestamp(null)
                .messageId(UUID.randomUUID())
                .plotUrl(null)
                .error(null)
                .data(new RetrievedData(
                        List.of("name", "email", "age"),
                        List.of(List.of("John", "john@gmail.com", "26"),
                                List.of("Lenny", "lenny@gmail.com", "65")),
                        0,
                        pageSize,
                        10L))
                .build();

        when(queryService.queryChat(databaseId, chatId, request, pageSize)).thenReturn(response);

        // then
        mockMvc.perform(post(ROOT_URL + "/{databaseId}/chat/{chatId}/query", databaseId, chatId, pageSize)
                        .param("pageSize", pageSize.toString())
                        .param("chatId", chatId.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(response), true));
    }

    @Test
    @DisplayName("Execute chat bad request")
    @WithMockUser(roles = "USER")
    void testExecuteChatBadRequest() throws Exception {
        // given
        Integer pageSize = 2;
        UUID databaseId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        QueryRequest request = new QueryRequest(null, null);
        // language=JSON
        String response = """
                {
                   "query":"must not be blank",
                   "model":"must not be null"
                }""";


        // then
        mockMvc.perform(post(ROOT_URL + "/{databaseId}/chat/{chatId}/query", databaseId, chatId, pageSize)
                        .param("pageSize", pageSize.toString())
                        .param("chatId", chatId.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json(response, true));
    }

    @Test
    @DisplayName("Execute chat by anonymous user")
    @WithAnonymousUser
    void testExecuteChatByAnotherUser() throws Exception {
        // given
        Integer pageSize = 2;
        UUID databaseId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        QueryRequest request = new QueryRequest("find all users older than 25", "gpt-4o");

        // then
        mockMvc.perform(post(ROOT_URL + "/{databaseId}/query/chat", databaseId, chatId, pageSize)
                        .param("pageSize", pageSize.toString())
                        .param("chatId", chatId.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Execute query-language query")
    @WithMockUser(roles = "USER")
    void testExecuteQueryLanguageQuery() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();
        Integer page = 0;
        Integer pageSize = 2;
        @SuppressWarnings("all")
        // language=SQL
        String query = """
                SELECT u.*, a.city, a.street, a.state
                FROM public."user" u
                JOIN public.address a ON u.id = a.user_id;
                """;
        ConsoleResponse response = ConsoleResponse.builder()
                .data(new RetrievedData(
                        List.of("id", "name", "age", "sex", "email", "created_at", "city", "street", "state"),
                        List.of(
                                List.of("1", "John Doe", "25", "M", "john.doe@example.com",
                                        "2024-05-26 07:52:41.545865", "Any town", "123 Main St", "CA"),
                                List.of("2", "Jane Smith", "30", "F", "jane.smith@example.com",
                                        "2024-05-26 07:52:41.545865", "Some town", "456 Oak Ave", "NY")),
                        page,
                        pageSize,
                        10L))
                .dbQuery(query)
                .error(null)
                .build();

        when(queryService.executeQueryLanguageSelectQuery(databaseId, query, page, pageSize)).thenReturn(response);

        // then
        mockMvc.perform(
                        post(ROOT_URL + "/{databaseId}/query/queryLanguage", databaseId)
                                .param("page", page.toString())
                                .param("pageSize", pageSize.toString())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.TEXT_PLAIN)
                                .content(query)
                                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(response), true));
    }

    @Test
    @DisplayName("Execute query-language query by anonymous user")
    @WithAnonymousUser
    void testExecuteQueryLanguageQueryByAnotherUser() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();
        int page = 0;
        int pageSize = 2;
        @SuppressWarnings("all")
        // language=SQL
        String query = """
                SELECT u.*, a.city, a.street, a.state
                FROM public."user" u
                JOIN public.address a ON u.id = a.user_id;
                """;

        // then
        mockMvc.perform(
                        post(ROOT_URL + "/{databaseId}/query/queryLanguage", databaseId)
                                .param("page", Integer.toString(page))
                                .param("pageSize", Integer.toString(pageSize))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.TEXT_PLAIN)
                                .content(query)
                                .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get database structure")
    @WithMockUser(roles = "USER")
    void testGetDatabaseStructure() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();
        SqlDatabaseStructureDto databaseStructure = new SqlDatabaseStructureDto(List.of(
                new SchemaDto("public",
                        List.of(
                                new TableDto("address", List.of(
                                        new Column("user_id", "integer", false,
                                                new SqlDatabaseStructure.ForeignKey(
                                                        "public",
                                                        "\"user\"",
                                                        "id")),
                                        new Column("city", "character varying", false),
                                        new Column("id", "integer", true)
                                ))))));

        when(databaseServiceMock.getDatabaseStructureByDatabaseId(databaseId)).thenReturn(databaseStructure);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/structure", databaseId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(databaseStructure), true));
    }

    @Test
    @DisplayName("Get database structure not found")
    @WithMockUser(roles = "USER")
    void testGetDatabaseStructureNotFound() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        when(databaseServiceMock.getDatabaseStructureByDatabaseId(databaseId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/structure", databaseId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get database structure by anonymous user")
    @WithAnonymousUser
    void testGetDatabaseStructureByAnotherUser() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/structure", databaseId))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get crate script")
    @WithMockUser(roles = "USER")
    void testGetCreateScript() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();
        @SuppressWarnings("all")
        // language=SQL
        String createScript = """
                CREATE SCHEMA IF NOT EXISTS "public";

                CREATE TABLE IF NOT EXISTS public.address
                (
                	id INTEGER PRIMARY KEY,
                	user_id INTEGER REFERENCES "user"(id),
                	city CHARACTER VARYING,
                	street CHARACTER VARYING,
                	state CHARACTER VARYING,
                	postal_code CHARACTER VARYING
                );

                CREATE TABLE IF NOT EXISTS public.example_table
                (
                	id INTEGER PRIMARY KEY,
                	name CHARACTER VARYING
                );""";

        when(databaseServiceMock.getDatabaseCreateScriptByDatabaseId(databaseId)).thenReturn(createScript);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/createScript", databaseId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(createScript));
    }

    @Test
    @DisplayName("Get crate script database not found")
    @WithMockUser(roles = "USER")
    void testGetDatabaseCreateScriptNotFound() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        when(databaseServiceMock.getDatabaseCreateScriptByDatabaseId(databaseId))
                .thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/createScript", databaseId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get crate script by anonymous user")
    @WithAnonymousUser
    void testGetDatabaseCreateScriptByAnotherUser() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/createScript", databaseId))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get chats of database")
    @WithMockUser(roles = "USER")
    void getChatsOfDatabase() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();
        List<ChatHistoryItem> response = List.of(
                new ChatHistoryItem(UUID.randomUUID(), "oldest user"),
                new ChatHistoryItem(UUID.randomUUID(), "New chat"));

        when(chatService.findChatsByDatabaseId(databaseId)).thenReturn(response);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/chats", databaseId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(response), true));
    }

    @Test
    @DisplayName("Get chats of not existing database")
    @WithMockUser(roles = "USER")
    void getChatsOfNotExistingDatabase() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        when(chatService.findChatsByDatabaseId(databaseId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/chats", databaseId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get chats of database by anonymous user")
    @WithAnonymousUser
    void getChatsOfDatabaseByAnotherUser() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/chats", databaseId))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}