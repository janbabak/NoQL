package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.LocalDatabaseTest;
import com.janbabak.noqlbackend.error.exception.*;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.database.CreateDatabaseRequest;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.query.*;
import com.janbabak.noqlbackend.model.user.RegisterRequest;
import com.janbabak.noqlbackend.service.chat.ChatTestUtilService;
import com.janbabak.noqlbackend.service.langchain.QueryDatabaseAssistantTools;
import com.janbabak.noqlbackend.service.langchain.QueryDatabaseLLMService;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import com.janbabak.noqlbackend.service.PlotService;
import com.janbabak.noqlbackend.service.query.QueryService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
    private QueryDatabaseLLMService llmApiServiceMock;

    @MockBean
    private PlotService plotServiceMock;

    @Autowired
    private DatabaseEntityService databaseService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatTestUtilService chatTestUtilService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private DatabaseCredentialsEncryptionService encryptionService;

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

        final RegisterRequest registerUserRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com")
                .password("password")
                .build();

        testUser = authenticationService.register(registerUserRequest, Role.ROLE_USER).user();

        AuthenticationService.authenticateUser(testUser);

        final CreateDatabaseRequest createPostgresRequest = CreateDatabaseRequest.builder()
                .name("Postgres database")
                .host(postgresDatabase.getHost())
                .port(postgresDatabase.getPort())
                .database(postgresDatabase.getDatabase())
                .userName(postgresDatabase.getUserName())
                .password(encryptionService.decryptCredentials(postgresDatabase.getPassword()))
                .engine(postgresDatabase.getEngine())
                .userId(testUser.getId())
                .build();

        final CreateDatabaseRequest createMysqlRequest = CreateDatabaseRequest.builder()
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
        // sql warnings
    void testExecuteQueryLanguageQuery(Database database)
            throws DatabaseConnectionException, BadRequestException, EntityNotFoundException {

        // given
        final UUID databaseId = database.getId();
        final Integer page = 1;
        final Integer pageSize = 5;
        // language=SQL
        final String query = "SELECT id, name, age, sex, email FROM eshop_user ORDER BY name;";

        final ConsoleResponse expectedResponse = ConsoleResponse.builder()
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
        final ConsoleResponse consoleResponse = queryService.executeQueryLanguageSelectQuery(
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
     * @param messages           list of messages to save into database
     * @param expectedResponse   expected response
     */
    @ParameterizedTest
    @MethodSource("testLoadChatResponseDataProvider")
    @DisplayName("Test load chat")
    void testLoadChatResponseData(
            Integer page,
            Integer pageSize,
            Long expectedTotalCount,
            List<ChatQueryWithResponse> messages,
            RetrievedData expectedResponse
    ) throws EntityNotFoundException {

        // given
        final UUID databaseId = getDatabase().getId();
        final ChatDto chat = chatService.create(databaseId);
        for (final ChatQueryWithResponse message : messages) {
            chatTestUtilService.addMessageToChat(chat.id(), message);
        }
        final ChatQueryWithResponse lastMessage = messages.get(messages.size() - 1);

        // when
        final RetrievedData queryResponse = queryService.getDataByMessageId(lastMessage.getId(), page, pageSize);

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
    Object[][] testLoadChatResponseDataProvider() {
        return new Object[][]{
                {
                        0, // page
                        10, // page size
                        2L, // expected total count
                        List.of( // messages
                                ChatQueryWithResponse.builder()
                                        .nlQuery("Find emails of all users")
                                        .resultDescription("Found emails of all users")
                                        .dbQuery("SELECT email FROM eshop_user;")
                                        .dbQueryExecutionSuccess(true)
                                        .dbExecutionErrorMessage(null)
                                        .plotScript(null)
                                        .plotGenerationSuccess(null)
                                        .plotGenerationErrorMessage(null)
                                        .build(),
                                ChatQueryWithResponse.builder()
                                        .nlQuery("Plot sex of users older than 24")
                                        .resultDescription("This bar chart shows users sex")
                                        .dbQuery("SELECT sex, COUNT(*) FROM eshop_user WHERE age > 24 GROUP BY sex")
                                        .dbQueryExecutionSuccess(true)
                                        .dbExecutionErrorMessage(null)
                                        .plotScript(FileUtils.getFileContent(
                                                "./src/test/resources/llmResponses/plotSexOfUsersSuccess.py"))
                                        .plotGenerationSuccess(true)
                                        .plotGenerationErrorMessage(null)
                                        .build()),
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
                        List.of(  // messages
                                ChatQueryWithResponse.builder()
                                        .nlQuery("Find emails of all users")
                                        .resultDescription("Found emails of all users")
                                        .dbQuery("SELECT email FROM eshop_user;")
                                        .dbQueryExecutionSuccess(true)
                                        .dbExecutionErrorMessage(null)
                                        .plotScript(null)
                                        .plotGenerationSuccess(null)
                                        .plotGenerationErrorMessage(null)
                                        .build(),
                                ChatQueryWithResponse.builder()
                                        .nlQuery("Sort them in descending order")
                                        .resultDescription("Emails soreted in descending order")
                                        .dbQuery("SELECT email FROM eshop_user ORDER BY email DESC;")
                                        .dbQueryExecutionSuccess(true)
                                        .dbExecutionErrorMessage(null)
                                        .plotScript(null)
                                        .plotGenerationSuccess(null)
                                        .plotGenerationErrorMessage(null)
                                        .build()),
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
     * @param pageSize         number of items per page
     * @param totalCount       total count of rows
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
            List<ChatQueryWithResponse> messages,
            QueryRequest request,
            QueryDatabaseLLMService.LLMServiceResult llmResponse,
            ChatResponse expectedResponse
    ) throws EntityNotFoundException, DatabaseConnectionException, DatabaseExecutionException, BadRequestException {

        // given
        final UUID databaseId = getDatabase().getId();
        final ChatDto chat = chatService.create(databaseId);
        for (final ChatQueryWithResponse message : messages) {
            chatTestUtilService.addMessageToChat(chat.id(), message);
        }

        when(llmApiServiceMock.executeUserRequest(any(QueryDatabaseLLMService.LLMServiceRequest.class)))
                .thenReturn(llmResponse);

        // when
        final ChatResponse actual = queryService.queryChat(databaseId, chat.id(), request, pageSize);

        // message id, timestamp, and plot url are generated, so we need to set them manually
        expectedResponse.setMessageId(actual.getMessageId());
        expectedResponse.setTimestamp(actual.getTimestamp());
        expectedResponse.setPlotUrl(actual.getPlotUrl());

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
    Object[][] testQueryChatWithPlotDataProvider() {
        final RetrievedData data1 = RetrievedData.builder()
                .page(0)
                .pageSize(8)
                .totalCount(2L)
                .columnNames(List.of("sex", "count"))
                .rows(List.of(
                        List.of("M", "9"),
                        List.of("F", "10")))
                .build();

        final RetrievedData data2 = RetrievedData.builder()
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
                .build();

        return new Object[][]{
                {
                        8, // page size
                        2L, // total count
                        List.of(), // messages
                        new QueryRequest("plot sex of users older than 24", "gpt-4o"),
                        // LLM response
                        new QueryDatabaseLLMService.LLMServiceResult(
                                "Successfully generated plot",
                                QueryDatabaseAssistantTools.QueryDatabaseToolResult.builder()
                                        .dbQueryExecutedSuccessSuccessfully(true)
                                        .dbQuery("SELECT sex, COUNT(*) FROM eshop_user WHERE age > 24 GROUP BY sex")
                                        .plotGeneratedSuccessfully(true)
                                        .script(FileUtils.getFileContent(
                                                "./src/test/resources/llmResponses/plotSexOfUsersSuccess.py"))
                                        .retrievedData(data1)
                                        .build()
                        ),
                        // expected response
                        ChatResponse.builder()
                                .messageId(null)
                                .nlQuery("plot sex of users older than 24")
                                .plotUrl(null)
                                .timestamp(null)
                                .description("Successfully generated plot")
                                .error(null)
                                .dbQuery("SELECT sex, COUNT(*) FROM eshop_user WHERE age > 24 GROUP BY sex")
                                .data(data1)
                                .build()
                },
                {
                        8, // page size
                        22L, // total count
                        List.of(
                                ChatQueryWithResponse.builder()
                                        .nlQuery("Find emails of all users")
                                        .resultDescription("Found emails of all users")
                                        .dbQuery("SELECT email FROM eshop_user;")
                                        .dbQueryExecutionSuccess(true)
                                        .dbExecutionErrorMessage(null)
                                        .plotScript(null)
                                        .plotGenerationSuccess(null)
                                        .plotGenerationErrorMessage(null)
                                        .build()),
                        // query request
                        new QueryRequest("sort them in descending order", "gpt-4o"),
                        // LLM response
                        new QueryDatabaseLLMService.LLMServiceResult(
                                "Users emails sorted in descending order",
                                QueryDatabaseAssistantTools.QueryDatabaseToolResult.builder()
                                        .dbQueryExecutedSuccessSuccessfully(true)
                                        .dbQuery("SELECT email FROM eshop_user ORDER BY email DESC;")
                                        .plotGeneratedSuccessfully(null)
                                        .script(null)
                                        .retrievedData(data2)
                                        .build()
                        ),
                        // expected response
                        ChatResponse.builder()
                                .messageId(null)
                                .plotUrl(null)
                                .timestamp(null)
                                .description("Users emails sorted in descending order")
                                .error(null)
                                .nlQuery("sort them in descending order")
                                .dbQuery("SELECT email FROM eshop_user ORDER BY email DESC;")
                                .data(data2)
                                .build()
                },
        };
    }

}
