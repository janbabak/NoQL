package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.LocalDatabaseTest;
import com.janbabak.noqlbackend.error.exception.*;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
import com.janbabak.noqlbackend.model.database.CreateDatabaseRequest;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.query.*;
import com.janbabak.noqlbackend.model.user.RegisterRequest;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import com.janbabak.noqlbackend.service.PlotService;
import com.janbabak.noqlbackend.service.QueryService;
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
    private PlotService plotServiceMock;

    @MockBean
    LlmApiServiceFactory llmApiServiceFactoryMock;

    @Autowired
    private DatabaseEntityService databaseService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private DatabaseCredentialsEncryptionService encryptionService;

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
                .password(encryptionService.decryptCredentials(postgresDatabase.getPassword()))
                .engine(postgresDatabase.getEngine())
                .userId(testUser.getId())
                .build();

        CreateDatabaseRequest createMysqlRequest = CreateDatabaseRequest.builder()
                .name("Postgres database")
                .host(mySqlDatabase.getHost())
                .port(mySqlDatabase.getPort())
                .database(mySqlDatabase.getDatabase())
                .userName(mySqlDatabase.getUserName())
                .password(encryptionService.decryptCredentials(mySqlDatabase.getPassword()))
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
    @SuppressWarnings("all")
        // IDE can't see the columns
    void testExecuteQueryLanguageQuery(Database database)
            throws DatabaseConnectionException, BadRequestException, EntityNotFoundException {

        // given
        UUID databaseId = database.getId();
        Integer page = 1;
        Integer pageSize = 5;
        // language=SQL
        String query = "SELECT id, name, age, sex, email FROM eshop_user ORDER BY name;";

        ConsoleResponse expectedResponse = ConsoleResponse.builder()
                .error(null)
                .dbQuery(query)
                .data(new RetrievedData(
                        List.of("id", "name", "age", "sex", "email"),
                        List.of(List.of("10", "David Taylor", "45", "M", "david.taylor@example.com"),
                                List.of("19", "Ella Thomas", "24", "F", "ella.thomas@example.com"),
                                List.of("5", "Emily Johnson", "40", "F", "emily.johnson@example.com"),
                                List.of("17", "Emma Scott", "30", "F", "emma.scott@example.com"),
                                List.of("21", "Grace Miller", "34", "F", "grace.miller@example.com")),
                        page,
                        pageSize,
                        22L))
                .build();

        // when
        ConsoleResponse consoleResponse = queryService.executeQueryLanguageSelectQuery(
                databaseId, query, page, pageSize);

        // then
        assertEquals(pageSize, consoleResponse.data().rows().size()); // page size
        assertEquals(22, consoleResponse.data().totalCount());
        assertEquals(expectedResponse, consoleResponse);
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
     * @param messageRequests    list of messages to save into database
     * @param expectedResponse   expected response
     */
    @ParameterizedTest
    @MethodSource("testLoadChatResponseDataProvider")
    @DisplayName("Test load chat")
    void testLoadChatResponseData(
            Integer page,
            Integer pageSize,
            Long expectedTotalCount,
            List<CreateChatQueryWithResponseRequest> messageRequests,
            RetrievedData expectedResponse
    ) throws EntityNotFoundException {

        // given
        UUID databaseId = getDatabase().getId();

        ChatDto chat = chatService.create(databaseId);
        List<ChatQueryWithResponse> messages = new ArrayList<>();
        for (CreateChatQueryWithResponseRequest messageRequest : messageRequests) {
            messages.add(chatService.addMessageToChat(chat.id(), messageRequest));
        }
        ChatQueryWithResponse lastMessage = messages.get(messages.size() - 1);

        // when
        RetrievedData queryResponse = queryService.getDataByMessageId(lastMessage.getId(), page, pageSize);

        // message id and timestamp are generated, so we need to set them manually

        // then
        assertTrue(pageSize >= queryResponse.rows().size());
        assertEquals(expectedTotalCount, queryResponse.totalCount());
        assertEquals(expectedResponse, queryResponse);

        // cleanup
        chatService.deleteChatById(chat.id());
    }

    /**
     * @return page, page size, expected total count, messages, expected response
     */
    @SuppressWarnings("all")
    // IDE can't see the columns
    Object[][] testLoadChatResponseDataProvider() {
        return new Object[][]{
                {
                        0, // page
                        10, // page size
                        2L, // expected total count
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
                        // expected response
                        RetrievedData.builder()
                                .page(0)
                                .pageSize(10)
                                .totalCount(2L)
                                .columnNames(List.of("sex", "count"))
                                .rows(List.of(
                                        List.of("M", "9"),
                                        List.of("F", "10")))
                                .build()
                },
                {
                        1, // page
                        10, // page size
                        22L, // expected total count
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
                        // expected reseponse
                        RetrievedData.builder()
                                .page(1)
                                .pageSize(10)
                                .totalCount(22L)
                                .columnNames(List.of("email"))
                                .rows(List.of(List.of("jane.doe@example.com"),
                                        List.of("james.wilson@example.com"),
                                        List.of("grace.miller@example.com"),
                                        List.of("emma.scott@example.com"),
                                        List.of("emily.johnson@example.com"),
                                        List.of("ella.thomas@example.com"),
                                        List.of("david.taylor@example.com"),
                                        List.of("daniel.miller@example.com"),
                                        List.of("christopher.johnson@example.com"),
                                        List.of("bob.smith@example.com")))
                                .build()
                }
        };
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
                        new RetrievedData(
                                List.of("sex", "count"),
                                List.of(List.of("M", "9"), List.of("F", "10")),
                                0,
                                10,
                                2L
                        )
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
                        new RetrievedData(
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
                                        List.of("bob.smith@example.com")),
                                1,
                                10,
                                22L
                        )
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
    @MethodSource("testQueryChatWithPlotDataProvider")
    @DisplayName("Test query chat - plot data provided")
    void testQueryChat(
            Integer pageSize,
            Long totalCount,
            Boolean plotResult,
            List<CreateChatQueryWithResponseRequest> messages,
            QueryRequest request,
            String llmResponse,
            ChatResponse expectedResponse
    ) throws EntityNotFoundException, DatabaseConnectionException, DatabaseExecutionException,
            LLMException, BadRequestException, PlotScriptExecutionException {

        // given
        UUID databaseId = getDatabase().getId();
        ChatDto chat = chatService.create(databaseId);
        for (CreateChatQueryWithResponseRequest message : messages) {
            chatService.addMessageToChat(chat.id(), message);
        }
        String plotFileName = "/static/images/" + chat.id() + "-unknown-message-id.png";

        when(llmApiServiceFactoryMock.getQueryApiService(eq("gpt-4o"))).thenReturn(queryApi);
        when(queryApi.queryModel(any(), eq(request), any(), eq(new ArrayList<>()))).thenReturn(llmResponse);
        when(plotServiceMock.generatePlot(any(), any(), eq(chat.id()), any())).thenReturn(plotFileName);

        // when
        ChatResponse actual = queryService.queryChat(databaseId, chat.id(), request, pageSize);

        // message id and timestamp are generated, so we need to set them manually
        expectedResponse.setMessageId(actual.getMessageId());
        expectedResponse.setTimestamp(actual.getTimestamp());

        if (plotResult) {
            expectedResponse.setPlotUrl(plotFileName);
        }

        // then
        assertTrue(pageSize >= actual.getData().rows().size());
        assertEquals(totalCount, actual.getData().totalCount());
        assertEquals(expectedResponse, actual);

        // cleanup
        chatService.deleteChatById(chat.id());
    }

    /**
     * @return page size, total count, plot result, messages, request, LLM response, expected response
     */
    @SuppressWarnings("all")
    // IDE can't see the columns
    Object[][] testQueryChatWithPlotDataProvider() {
        return new Object[][]{
                {
                        8, // page size
                        2L, // total count
                        true, // plot result
                        List.of(), // messages
                        // query request
                        new QueryRequest("plot sex of users older than 24", "gpt-4o"),
                        // LLM response
                        FileUtils.getFileContent("./src/test/resources/llmResponses/plotSexOfUsersSuccess.json"),
                        // expected response
                        ChatResponse.builder()
                                .messageId(null)
                                .nlQuery("plot sex of users older than 24")
                                .plotUrl(null)
                                .timestamp(null)
                                .error(null)
                                .dbQuery(
                                        // language=SQL
                                        "SELECT sex, COUNT(*) FROM eshop_user WHERE age > 24 GROUP BY sex")
                                .data(RetrievedData.builder()
                                        .page(0)
                                        .pageSize(8)
                                        .totalCount(2L)
                                        .columnNames(List.of("sex", "count"))
                                        .rows(List.of(
                                                List.of("M", "9"),
                                                List.of("F", "10")))
                                        .build())
                                .build()
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
                        new QueryRequest("sort them in descending order", "gpt-4o"),
                        // language=JSON LLM response
                        """
                        {
                            "databaseQuery": "SELECT email FROM eshop_user ORDER BY email DESC;",
                            "generatePlot": false
                        }""",
                        // expected response
                        ChatResponse.builder()
                                .messageId(null)
                                .plotUrl(null)
                                .timestamp(null)
                                .error(null)
                                .nlQuery("sort them in descending order")
                                .dbQuery(
                                        // language=SQL
                                        "SELECT email FROM eshop_user ORDER BY email DESC;")
                                .data(RetrievedData.builder()
                                        .page(0)
                                        .pageSize(8)
                                        .totalCount(22L)
                                        .columnNames(List.of("email"))
                                        .rows(List.of(List.of("william.davis@example.com"),
                                                List.of("sophia.lopez@example.com"),
                                                List.of("sarah.brown@example.com"),
                                                List.of("olivia.garcia@example.com"),
                                                List.of("nicholas.brown@example.com"),
                                                List.of("michael.davis@example.com"),
                                                List.of("matthew.hernandez@example.com"),
                                                List.of("john.doe@example.com")))
                                        .build())
                                .build()
                },
        };
    }
}
