package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatHistoryItem;
import com.janbabak.noqlbackend.model.chat.ChatQueryWithResponseDto;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Column;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructureDto;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructureDto.SchemaDto;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructureDto.TableDto;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.model.query.QueryResponse;
import com.janbabak.noqlbackend.model.query.gpt.LlmModel;
import com.janbabak.noqlbackend.service.chat.ChatService;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.database.DatabaseEntityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.janbabak.noqlbackend.service.utils.JsonUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DatabaseController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatabaseEntityService databaseService;

    @MockBean
    @SuppressWarnings("unused") // mock is used internally
    private QueryService queryService;

    @MockBean
    private ChatService chatService;

    private final String ROOT_URL = "/database";

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
            .build();

    @Test
    @DisplayName("Get all databases")
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
                .build();
        List<Database> databases = List.of(localPostgres, localMysql);

        // when
        when(databaseService.findAll()).thenReturn(databases);

        // then
        mockMvc.perform(get(ROOT_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(databases), true));
    }

    @Test
    @DisplayName("Get database by id")
    void testGetDatabaseById() throws Exception {
        // when
        when(databaseService.findById(localPostgres.getId())).thenReturn(localPostgres);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}", localPostgres.getId()))
                .andDo(print())
                .andExpect(content().json(toJson(localPostgres), true))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get database by id not found")
    void testGetDatabaseByIdNotFound() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        // when
        when(databaseService.findById(databaseId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}", databaseId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @DisplayName("Create database")
    @MethodSource("createDatabaseDataProvider")
    void testCreateDatabase(String request, String response, Boolean success) throws Exception {
        // when
        if (success) {
            when(databaseService.create(any())).thenReturn(createFromJson(response, Database.class));
        }

        // then
        mockMvc.perform(post(ROOT_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(print())
                .andExpect(success ? status().isCreated() : status().isBadRequest())
                .andExpect(content().json(response, true));
    }

    /**
     * @return request, response, and success
     */
    Object[][] createDatabaseDataProvider() {
        return new Object[][]{
                {
                        // language=JSON
                        """
                    {
                        "name":"Local Postgres",
                        "host":"localhost",
                        "port":5432,
                        "database":"database",
                        "userName":"user",
                        "password":"password",
                        "engine":"POSTGRES"
                    }""",
                        // language=JSON
                        """
                    {
                        "id": "6678fc72-1a55-4146-b74b-b3f5aac677df",
                        "name":"Local Postgres",
                        "host":"localhost",
                        "port":5432,
                        "database":"database",
                        "userName":"user",
                        "password":"password",
                        "engine":"POSTGRES"
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
                        "host":"localhost",
                        "port":5432,
                        "database": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        "userName":"user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user user user",
                        "password":"password_password_password_password_password_password_password_password_password _password_password_password_password_password_password_password_password_password_password password_password_password_password_password_password_password_password_password_password password_password_password_password_password_password_password_password_password_password password_password_password_password_password_password_password_password_password_password",
                        "engine":"POSTGRES"
                    }""",
                        // language=JSON
                        """
                    {
                         "name": "length must be between 1 and 32",
                         "database":"length must be between 1 and 253",
                         "userName":"length must be between 1 and 128",
                         "password":"length must be between 1 and 128"
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
                        // language=JSON
                        """
                    {
                          "name": "must not be blank",
                          "host": "must not be blank",
                          "database": "must not be blank",
                          "userName": "must not be blank",
                          "password": "must not be blank"
                      }""",
                        false
                }
        };
    }

    @ParameterizedTest
    @DisplayName("Update database")
    @MethodSource("updateDatabaseDataProvider")
    void testUpdateDatabase(String request, String response, Boolean success) throws Exception {
        // given
        UUID databaseId = UUID.fromString("6678fc72-1a55-4146-b74b-b3f5aac677df");

        // when
        if (success) {
            when(databaseService.update(eq(databaseId), any())).thenReturn(createFromJson(response, Database.class));
        }

        // then
        mockMvc.perform(put(ROOT_URL + "/{databaseId}", databaseId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(print())
                .andExpect(success ? status().isOk() : status().isBadRequest())
                .andExpect(content().json(response, true));
    }

    /**
     * @return request, response, and success flag
     */
    Object[][] updateDatabaseDataProvider() {
        return new Object[][]{
                {
                        // language=JSON
                        """
                    {
                        "name":"Updated name"
                    }""",
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
                        "engine":"POSTGRES"
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
                        "engine":"MYSQL"
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
                        "host":"localhost",
                        "port":5432,
                        "database": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        "userName":"user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user_user user user",
                        "password":"password_password_password_password_password_password_password_password_password _password_password_password_password_password_password_password_password_password_password password_password_password_password_password_password_password_password_password_password password_password_password_password_password_password_password_password_password_password password_password_password_password_password_password_password_password_password_password",
                        "engine":"POSTGRES"
                    }""",
                        // language=JSON
                        """
                    {
                         "name": "length must be between 1 and 32",
                         "database":"length must be between 1 and 253",
                         "userName":"length must be between 1 and 128",
                         "password":"length must be between 1 and 128"
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
                        // language=JSON
                        """
                    {
                        "name": "length must be between 1 and 32",
                        "host": "length must be between 1 and 253",
                        "database": "length must be between 1 and 253",
                        "userName": "length must be between 1 and 128",
                        "password": "length must be between 1 and 128"
                    }""",
                        false
                }
        };
    }

    @Test
    @DisplayName("Delete database")
    void testDeleteDatabaseById() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        // then
        mockMvc.perform(delete(ROOT_URL + "/{databaseId}", databaseId))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Execute chat")
    void testExecuteChat() throws Exception {

        // given
        Integer pageSize = 2;
        UUID databaseId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        QueryRequest request = new QueryRequest(chatId, "find all users older than 25", LlmModel.GPT_4o);
        QueryResponse response = QueryResponse.builder()
                .totalCount(10L)
                .errorMessage(null)
                .data(new QueryResponse.RetrievedData(
                        List.of("name", "email", "age"),
                        List.of(
                                List.of("John", "john@gmail.com", "26"),
                                List.of("Lenny", "lenny@gmail.com", "65"))))
                .chatQueryWithResponse(ChatQueryWithResponseDto.builder()
                        .id(UUID.randomUUID())
                        .nlQuery("find all users older than 25")
                        .timestamp(null)
                        .llmResult(new ChatQueryWithResponseDto.LLMResult(
                                "SELECT * FROM users WHERE age > 25", null))
                        .build())
                .build();

        // when
        when(queryService.executeChat(databaseId, request, pageSize)).thenReturn(response);

        // then
        mockMvc.perform(post(ROOT_URL + "/{databaseId}/query/chat", databaseId, pageSize)
                        .param("pageSize", pageSize.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(response), true));
    }

    @Test
    @DisplayName("Execute chat bad request")
    void testExecuteChatBadRequest() throws Exception {
        // given
        Integer pageSize = 2;
        UUID databaseId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        QueryRequest request = new QueryRequest(chatId, null, null);
        // language=JSON
        String response = """
                {
                   "query":"must not be blank",
                   "model":"must not be null"
                }""";


        // then
        mockMvc.perform(post(ROOT_URL + "/{databaseId}/query/chat", databaseId, pageSize)
                        .param("pageSize", pageSize.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json(response, true));
    }

    @Test
    @DisplayName("Load chat result")
    void testLoadChatResult() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        Integer page = 1;
        Integer pageSize = 2;
        QueryResponse response = QueryResponse.builder()
                .totalCount(10L)
                .errorMessage(null)
                .data(new QueryResponse.RetrievedData(
                        List.of("name", "email", "age"),
                        List.of(
                                List.of("John", "john@gmail.com", "26"),
                                List.of("Lenny", "lenny@gmail.com", "65"))))
                .chatQueryWithResponse(ChatQueryWithResponseDto.builder()
                        .id(UUID.randomUUID())
                        .nlQuery("find all users older than 25")
                        .timestamp(null)
                        .llmResult(new ChatQueryWithResponseDto.LLMResult(
                                "SELECT * FROM users WHERE age > 25", null))
                        .build())
                .build();

        // when
        when(queryService.loadChatResult(databaseId, chatId, page, pageSize)).thenReturn(response);

        // then
        mockMvc.perform(
                get(ROOT_URL + "/{databaseId}/query/loadChatResult", databaseId, chatId, page, pageSize)
                        .param("page", page.toString())
                        .param("pageSize", pageSize.toString())
                        .param("chatId", chatId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(response), true));
    }

    @Test
    @DisplayName("Execute query-language query")
    void testExecuteQueryLanguageQuery() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();
        Integer page = 0;
        Integer pageSize = 2;
        // language=SQL
        String query = """
                SELECT u.*, a.city, a.street, a.state
                FROM public."user" u
                JOIN public.address a ON u.id = a.user_id;
                """;
        QueryResponse response = QueryResponse.builder()
                .totalCount(10L)
                .errorMessage(null)
                .chatQueryWithResponse(null)
                .data(new QueryResponse.RetrievedData(
                        List.of("id", "name", "age", "sex", "email", "created_at", "city", "street", "state"),
                        List.of(
                                List.of("1", "John Doe", "25", "M", "john.doe@example.com",
                                        "2024-05-26 07:52:41.545865", "Any town", "123 Main St", "CA"),
                                List.of("2", "Jane Smith", "30", "F", "jane.smith@example.com",
                                        "2024-05-26 07:52:41.545865", "Some town", "456 Oak Ave", "NY"))))
                .build();

        // when
        when(queryService.executeQueryLanguageSelectQuery(databaseId, query, page, pageSize)).thenReturn(response);

        // then
        mockMvc.perform(
                post(ROOT_URL + "/{databaseId}/query/queryLanguage", databaseId)
                        .param("page", page.toString())
                        .param("pageSize", pageSize.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(query))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(response), true));
    }

    @Test
    @DisplayName("Get database structure")
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

        // when
        when(databaseService.getDatabaseStructureByDatabaseId(databaseId)).thenReturn(databaseStructure);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/structure", databaseId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(databaseStructure), true));
    }

    @Test
    @DisplayName("Get database structure not found")
    void testGetDatabaseStructureNotFound() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        // when
        when(databaseService.getDatabaseStructureByDatabaseId(databaseId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/structure", databaseId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get crate script")
    void testGetCreateScript() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();
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

        // when
        when(databaseService.getDatabaseCreateScriptByDatabaseId(databaseId)).thenReturn(createScript);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/createScript", databaseId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(createScript));
    }

    @Test
    @DisplayName("Get crate script database not found")
    void testGetDatabaseCreateScriptNotFound() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        // when
        when(databaseService.getDatabaseCreateScriptByDatabaseId(databaseId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/createScript", databaseId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get chats of database")
    void getChatsOfDatabase() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();
        List<ChatHistoryItem> response = List.of(
                new ChatHistoryItem(UUID.randomUUID(), "oldest user"),
                new ChatHistoryItem(UUID.randomUUID(), "New chat"));

        // when
        when(chatService.findChatsByDatabaseId(databaseId)).thenReturn(response);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/chats", databaseId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(response), true));
    }

    @Test
    @DisplayName("Get chats of not existing database")
    void getChatsOfNotExistingDatabase() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        // when
        when(chatService.findChatsByDatabaseId(databaseId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{databaseId}/chats", databaseId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}