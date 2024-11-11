package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.LocalDatabaseTest;
import com.janbabak.noqlbackend.error.exception.*;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.ChatQueryWithResponseDto;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
import com.janbabak.noqlbackend.model.database.CreateDatabaseRequest;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.model.query.QueryResponse;
import com.janbabak.noqlbackend.model.user.RegisterRequest;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import com.janbabak.noqlbackend.service.PlotService;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.api.LlmApiServiceFactory;
import com.janbabak.noqlbackend.service.api.QueryApi;
import com.janbabak.noqlbackend.service.chat.ChatService;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link QueryService}. Tests the interaction between the service and the database.
 * PlotService had to be mocked because connection problems between containers.
 */
@SpringBootTest
@ActiveProfiles("test")
public class QueryServiceIntegrationTest extends LocalDatabaseTest {

    private Database getDatabase() {
        return postgresDatabase;
    }

    @InjectMocks
    @Autowired
    private QueryService queryService;

    @MockBean
    private PlotService plotService;

    @MockBean
    LlmApiServiceFactory llmApiServiceFactory;

    @Autowired
    private DatabaseEntityService databaseService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private AuthenticationService authenticationService;

    QueryApi queryApi = Mockito.mock(QueryApi.class);

    private User testUser;

    /**
     * Get scripts for initialization of the databases
     */
    @Override
    protected Scripts getInitializationScripts() {
        return new Scripts(
                FileUtils.getFileContent("./src/test/resources/dbScripts/postgres/eshopUser.sql"),
                FileUtils.getFileContent("./src/test/resources/dbScripts/mySql/eshopUser.sql"));
    }

    /**
     * Get scripts for cleanup of the databases.
     */
    @Override
    protected Scripts getCleanupScript() {
        return new Scripts(
                FileUtils.getFileContent("./src/test/resources/dbScripts/postgres/eshopUserCleanup.sql"),
                FileUtils.getFileContent("./src/test/resources/dbScripts/mySql/eshopUserCleanup.sql"));
    }

    @BeforeAll
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        RegisterRequest registerUserRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com")
                .password("password")
                .build();

        testUser = authenticationService.register(registerUserRequest, Role.ROLE_USER).user();

        AuthenticationService.authenticateUser(testUser);

        CreateDatabaseRequest createPostgresRequest = CreateDatabaseRequest.builder()
                .name("Postgres database")
                .host(postgresDatabase.getHost())
                .port(postgresDatabase.getPort())
                .database(postgresDatabase.getDatabase())
                .userName(postgresDatabase.getUserName())
                .password(postgresDatabase.getPassword())
                .engine(postgresDatabase.getEngine())
                .userId(testUser.getId())
                .build();

        CreateDatabaseRequest createMysqlRequest = CreateDatabaseRequest.builder()
                .name("Postgres database")
                .host(mySqlDatabase.getHost())
                .port(mySqlDatabase.getPort())
                .database(mySqlDatabase.getDatabase())
                .userName(mySqlDatabase.getUserName())
                .password(mySqlDatabase.getPassword())
                .engine(mySqlDatabase.getEngine())
                .userId(testUser.getId())
                .build();

        postgresDatabase = databaseService.create(createPostgresRequest);
        mySqlDatabase = databaseService.create(createMysqlRequest);
    }

    @AfterAll
    @Override
    protected void tearDown() throws DatabaseConnectionException, DatabaseExecutionException {
        super.tearDown();

        AuthenticationService.authenticateUser(testUser);

        databaseService.deleteById(postgresDatabase.getId());
        databaseService.deleteById(mySqlDatabase.getId());
    }

    @BeforeEach
    protected void beforeEach() {
        AuthenticationService.authenticateUser(testUser);
    }

    @ParameterizedTest
    @MethodSource("databaseDataProvider")
    @DisplayName("Test execute query language query")
    @SuppressWarnings("all") // IDE can't see the columns
    void testExecuteQueryLanguageQuery(Database database)
            throws DatabaseConnectionException, BadRequestException, EntityNotFoundException {

        // given
        UUID databaseId = database.getId();
        Integer page = 1;
        Integer pageSize = 5;
        // language=SQL
        String query = "SELECT id, name, age, sex, email FROM eshop_user ORDER BY name;";

        QueryResponse expectedResponse = new QueryResponse(
                new QueryResponse.RetrievedData(
                        List.of("id", "name", "age", "sex", "email"),
                        List.of(List.of("10", "David Taylor", "45", "M", "david.taylor@example.com"),
                                List.of("19", "Ella Thomas", "24", "F", "ella.thomas@example.com"),
                                List.of("5", "Emily Johnson", "40", "F", "emily.johnson@example.com"),
                                List.of("17", "Emma Scott", "30", "F", "emma.scott@example.com"),
                                List.of("21", "Grace Miller", "34", "F", "grace.miller@example.com"))),
                22L,
                null,
                null);

        // when
        QueryResponse queryResponse = queryService.executeQueryLanguageSelectQuery(databaseId, query, page, pageSize);

        // then
        assertEquals(pageSize, queryResponse.data().rows().size()); // page size
        assertEquals(22, queryResponse.totalCount());
        assertEquals(expectedResponse, queryResponse);
    }

    Object[] databaseDataProvider() {
        return new Object[]{
                postgresDatabase,
                mySqlDatabase
        };
    }

    /**
     * @param page               page number
     * @param pageSize           number of items per page
     * @param expectedTotalCount total count of rows
     * @param plotResult         if the result contains plot
     * @param messageRequests    list of messages to save into database
     * @param expectedResponse   expected response
     */
    @ParameterizedTest
    @MethodSource("testLoadChatDataProvider")
    @DisplayName("Test load chat")
    void testLoadChatResults(
            Integer page,
            Integer pageSize,
            Long expectedTotalCount,
            Boolean plotResult,
            List<CreateChatQueryWithResponseRequest> messageRequests,
            QueryResponse expectedResponse
    ) throws EntityNotFoundException, DatabaseConnectionException, BadRequestException {

        // given
        UUID databaseId = getDatabase().getId();
        ChatDto chat = chatService.create(databaseId);
        List<ChatQueryWithResponse> messages = new ArrayList<>();
        for (CreateChatQueryWithResponseRequest messageRequest : messageRequests) {
            messages.add(chatService.addMessageToChat(chat.id(), messageRequest));
        }

        // when
        QueryResponse queryResponse = queryService.loadChatResult(databaseId, chat.id(), page, pageSize);

        // message id and timestamp are generated, so we need to set them manually
        ChatQueryWithResponse lastMessage = messages.get(messages.size() - 1);
        expectedResponse.chatQueryWithResponse().setId(lastMessage.getId());
        expectedResponse.chatQueryWithResponse().setTimestamp(
                queryResponse.chatQueryWithResponse().getTimestamp());
        if (plotResult) {
            expectedResponse.chatQueryWithResponse().getLlmResult().setPlotUrl(
                    "/static/images/" + chat.id() + ".png");
        }

        // then
        assertTrue(pageSize >= queryResponse.data().rows().size());
        assertEquals(expectedTotalCount, queryResponse.totalCount());
        assertEquals(expectedResponse, queryResponse);

        // cleanup
        chatService.deleteChatById(chat.id());
    }

    /**
     * @return page, page size, expected total count, plot result, messages, expected response
     */
    @SuppressWarnings("all")
    // IDE can't see the columns
    Object[][] testLoadChatDataProvider() {
        return new Object[][]{
                {
                        0, // page
                        10, // page size
                        2L, // expected total count
                        true, // plot result
                        List.of( // messages
                                new CreateChatQueryWithResponseRequest(
                                        "find emails of all users",
                                        // language=JSON
                                        """
                                                {
                                                    "databaseQuery": "SELECT email FROM eshop_user;",
                                                    "generatePlot": false,
                                                    "pythonCode": ""
                                                }"""),
                                new CreateChatQueryWithResponseRequest(
                                        "plot sex of users older than 24",
                                        FileUtils.getFileContent(
                                                "./src/test/resources/llmResponses/plotSexOfUsersSuccess.json"))),
                        new QueryResponse( // expected response
                                new QueryResponse.RetrievedData(
                                        List.of("sex", "count"),
                                        List.of(List.of("M", "9"), List.of("F", "10"))),
                                2L,
                                new ChatQueryWithResponseDto(
                                        null,
                                        "plot sex of users older than 24",
                                        new ChatQueryWithResponseDto.LLMResult(
                                                // language=SQL
                                                "SELECT sex, COUNT(*) FROM eshop_user WHERE age > 24 GROUP BY sex",
                                                null),
                                        null),
                                null)
                },
                {
                        1, // page
                        10, // page size
                        22L, // expected total count
                        false, // plot result
                        List.of(new CreateChatQueryWithResponseRequest( // messages
                                        "find emails of all users",
                                        // language=JSON
                                        """
                                                {
                                                    "databaseQuery": "SELECT email FROM eshop_user;",
                                                    "generatePlot": false,
                                                    "pythonCode": ""
                                                }"""),
                                new CreateChatQueryWithResponseRequest(
                                        "sort them in descending order",
                                        // language=JSON
                                        """
                                                {
                                                    "databaseQuery": "SELECT email FROM eshop_user ORDER BY email DESC;",
                                                    "generatePlot": false,
                                                    "pythonCode": ""
                                                }""")),
                        new QueryResponse( // expected response
                                new QueryResponse.RetrievedData(
                                        List.of("email"),
                                        List.of(List.of("jane.doe@example.com"),
                                                List.of("james.wilson@example.com"),
                                                List.of("grace.miller@example.com"),
                                                List.of("emma.scott@example.com"),
                                                List.of("emily.johnson@example.com"),
                                                List.of("ella.thomas@example.com"),
                                                List.of("david.taylor@example.com"),
                                                List.of("daniel.miller@example.com"),
                                                List.of("christopher.johnson@example.com"),
                                                List.of("bob.smith@example.com"))),
                                22L,
                                new ChatQueryWithResponseDto(
                                        null,
                                        "sort them in descending order",
                                        new ChatQueryWithResponseDto.LLMResult(
                                                // language=SQL
                                                "SELECT email FROM eshop_user ORDER BY email DESC;",
                                                null),
                                        null),
                                null)

                }
        };
    }

    /**
     * @param pageSize         number of items per page
     * @param totalCount       total count of rows
     * @param plotResult       if the result contains plot
     * @param messages         messages to save into database
     * @param request          query request - natural language query
     * @param llmResponse      LLM response
     * @param expectedResponse expected response
     */
    @ParameterizedTest
    @MethodSource("testExecuteChatWithPlotDataProvider")
    @DisplayName("Test execute chat")
    void testExecuteChat(
            Integer pageSize,
            Long totalCount,
            Boolean plotResult,
            List<CreateChatQueryWithResponseRequest> messages,
            QueryRequest request,
            String llmResponse,
            QueryResponse expectedResponse
    ) throws EntityNotFoundException, DatabaseConnectionException, DatabaseExecutionException,
            LLMException, BadRequestException, PlotScriptExecutionException {

        // given
        UUID databaseId = getDatabase().getId();
        ChatDto chat = chatService.create(databaseId);
        request.setChatId(chat.id());
        for (CreateChatQueryWithResponseRequest message : messages) {
            chatService.addMessageToChat(chat.id(), message);
        }

        when(llmApiServiceFactory.getQueryApiService(eq("gpt-4o"))).thenReturn(queryApi);
        when(queryApi.queryModel(any(), eq(request), any(), eq(new ArrayList<>()))).thenReturn(llmResponse);
        doNothing().when(plotService).generatePlot(any(), any(), any());

        // when
        QueryResponse queryResponse = queryService.executeChat(databaseId, request, pageSize);

        // message id and timestamp are generated, so we need to set them manually
        expectedResponse.chatQueryWithResponse().setId(
                queryResponse.chatQueryWithResponse().getId());
        expectedResponse.chatQueryWithResponse().setTimestamp(
                queryResponse.chatQueryWithResponse().getTimestamp());
        if (plotResult) {
            expectedResponse.chatQueryWithResponse().getLlmResult().setPlotUrl(
                    "/static/images/" + chat.id() + ".png");
        }

        // then
        assertTrue(pageSize >= queryResponse.data().rows().size());
        assertEquals(totalCount, queryResponse.totalCount());
        assertEquals(expectedResponse, queryResponse);

        // cleanup
        chatService.deleteChatById(chat.id());
    }

    /**
     * @return page size, total count, plot result, messages, request, LLM response, expected response
     */
    @SuppressWarnings("all")
    // IDE can't see the columns
    Object[][] testExecuteChatWithPlotDataProvider() {
        return new Object[][]{
                {
                        8, // page size
                        2L, // total count
                        true, // plot result
                        List.of(), // messages
                        // query request
                        new QueryRequest(null, "plot sex of users older than 24", "gpt-4o"),
                        // LLM response
                        FileUtils.getFileContent("./src/test/resources/llmResponses/plotSexOfUsersSuccess.json"),
                        // expected response
                        new QueryResponse(
                                new QueryResponse.RetrievedData(
                                        List.of("sex", "count"),
                                        List.of(List.of("M", "9"), List.of("F", "10"))),
                                2L,
                                new ChatQueryWithResponseDto(
                                        null,
                                        "plot sex of users older than 24",
                                        new ChatQueryWithResponseDto.LLMResult(
                                                // language=SQL
                                                "SELECT sex, COUNT(*) FROM eshop_user WHERE age > 24 GROUP BY sex",
                                                null),
                                        null),
                                null)
                },
                {
                        8, // page size
                        22L, // total count
                        false, // plot result
                        List.of(new CreateChatQueryWithResponseRequest( // messages
                                "find emails of all users",
                                // language=JSON
                                """
                                        {
                                            "databaseQuery": "SELECT email FROM eshop_user;",
                                            "generatePlot": false,
                                            "pythonCode": ""
                                        }""")),
                        // query request
                        new QueryRequest(null, "sort them in descending order", "gpt-4o"),
                        // language=JSON LLM response
                        """
                        {
                            "databaseQuery": "SELECT email FROM eshop_user ORDER BY email DESC;",
                            "generatePlot": false
                        }""",
                        // expected response
                        new QueryResponse(
                                new QueryResponse.RetrievedData(
                                        List.of("email"),
                                        List.of(List.of("william.davis@example.com"),
                                                List.of("sophia.lopez@example.com"),
                                                List.of("sarah.brown@example.com"),
                                                List.of("olivia.garcia@example.com"),
                                                List.of("nicholas.brown@example.com"),
                                                List.of("michael.davis@example.com"),
                                                List.of("matthew.hernandez@example.com"),
                                                List.of("john.doe@example.com"))),
                                22L,
                                new ChatQueryWithResponseDto(
                                        null,
                                        "sort them in descending order",
                                        new ChatQueryWithResponseDto.LLMResult(
                                                // language=SQL
                                                "SELECT email FROM eshop_user ORDER BY email DESC;",
                                                null),
                                        null),
                                null)

                },
        };
    }
}
