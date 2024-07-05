package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.LocalDatabaseTest;
import com.janbabak.noqlbackend.error.exception.*;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.ChatQueryWithResponseDto;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.model.query.QueryResponse;
import com.janbabak.noqlbackend.model.query.gpt.LlmModel;
import com.janbabak.noqlbackend.service.PlotService;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.api.LlmApiServiceFactory;
import com.janbabak.noqlbackend.service.api.QueryApi;
import com.janbabak.noqlbackend.service.chat.ChatService;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
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

    @Autowired
    private DatabaseEntityService databaseService;

    @Autowired
    private ChatService chatService;

    MockedStatic<LlmApiServiceFactory> apiServiceMock = Mockito.mockStatic(LlmApiServiceFactory.class);

    QueryApi queryApi = Mockito.mock(QueryApi.class);

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
    protected void setUp() throws DatabaseConnectionException, DatabaseExecutionException {
        super.setUp();

        databaseService.create(postgresDatabase);
        databaseService.create(mySqlDatabase);

        apiServiceMock
                .when(() -> LlmApiServiceFactory.getQueryApiService(LlmModel.GPT_4o))
                .thenReturn(queryApi);
    }

    @AfterAll
    @Override
    protected void tearDown() throws DatabaseConnectionException, DatabaseExecutionException {
        super.tearDown();

        databaseService.deleteById(postgresDatabase.getId());
        databaseService.deleteById(mySqlDatabase.getId());

        apiServiceMock.close();
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
        assertEquals(pageSize, queryResponse.getData().getRows().size()); // page size
        assertEquals(22, queryResponse.getTotalCount());
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
            messages.add(chatService.addMessageToChat(chat.getId(), messageRequest));
        }

        // when
        QueryResponse queryResponse = queryService.loadChatResult(databaseId, chat.getId(), page, pageSize);

        // message id and timestamp are generated, so we need to set them manually
        ChatQueryWithResponse lastMessage = messages.get(messages.size() - 1);
        expectedResponse.getChatQueryWithResponse().setId(lastMessage.getId());
        expectedResponse.getChatQueryWithResponse().setTimestamp(
                queryResponse.getChatQueryWithResponse().getTimestamp());
        if (plotResult) {
            expectedResponse.getChatQueryWithResponse().getLlmResult().setPlotUrl(
                    "/static/images/" + chat.getId() + ".png");
        }

        // then
        assertTrue(pageSize >= queryResponse.getData().getRows().size());
        assertEquals(expectedTotalCount, queryResponse.getTotalCount());
//        log.info("expected response: {}", expectedResponse);
//        log.info("query response: {}", queryResponse);
        assertEquals(expectedResponse, queryResponse);

        // cleanup
        chatService.deleteChatById(chat.getId());
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
                                        List.of(List.of("M", "11"), List.of("F", "11"))),
                                2L,
                                new ChatQueryWithResponseDto(
                                        null,
                                        "plot sex of users older than 24",
                                        new ChatQueryWithResponseDto.LLMResult(
                                                // language=SQL
                                                "SELECT sex, COUNT(*) FROM eshop_user WHERE age > 4 GROUP BY sex",
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
        request.setChatId(chat.getId());
        for (CreateChatQueryWithResponseRequest message : messages) {
            chatService.addMessageToChat(chat.getId(), message);
        }

        // when
        when(queryApi.queryModel(any(), eq(request), any(), eq(new ArrayList<>()))).thenReturn(llmResponse);

        doNothing().when(plotService).generatePlot(any(), any(), any());

        QueryResponse queryResponse = queryService.executeChat(databaseId, request, pageSize);

        // message id and timestamp are generated, so we need to set them manually
        expectedResponse.getChatQueryWithResponse().setId(
                queryResponse.getChatQueryWithResponse().getId());
        expectedResponse.getChatQueryWithResponse().setTimestamp(
                queryResponse.getChatQueryWithResponse().getTimestamp());
        if (plotResult) {
            expectedResponse.getChatQueryWithResponse().getLlmResult().setPlotUrl(
                    "/static/images/" + chat.getId() + ".png");
        }

        // then
        System.out.println("query response");
        System.out.println(queryResponse);
        assertTrue(pageSize >= queryResponse.getData().getRows().size());
        assertEquals(totalCount, queryResponse.getTotalCount());
        assertEquals(expectedResponse, queryResponse);

        // cleanup
        chatService.deleteChatById(chat.getId());
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
                        2L, // total countjanbabak/plot-service
                        true, // plot result
                        List.of(), // messages
                        // query request
                        new QueryRequest(null, "plot sex of users older than 24", LlmModel.GPT_4o),
                        // LLM response
                        FileUtils.getFileContent("./src/test/resources/llmResponses/plotSexOfUsersSuccess.json"),
                        // expected response
                        new QueryResponse(
                                new QueryResponse.RetrievedData(
                                        List.of("sex", "count"),
                                        List.of(List.of("M", "11"), List.of("F", "11"))),
                                2L,
                                new ChatQueryWithResponseDto(
                                        null,
                                        "plot sex of users older than 24",
                                        new ChatQueryWithResponseDto.LLMResult(
                                                // language=SQL
                                                "SELECT sex, COUNT(*) FROM eshop_user WHERE age > 4 GROUP BY sex",
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
                        new QueryRequest(null, "sort them in descending order", LlmModel.GPT_4o),
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
