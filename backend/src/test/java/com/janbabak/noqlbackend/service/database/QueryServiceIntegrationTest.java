package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.PostgresTest;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.ChatQueryWithResponseDto;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.model.query.QueryResponse;
import com.janbabak.noqlbackend.model.query.gpt.LlmModel;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.api.LlmApiServiceFactory;
import com.janbabak.noqlbackend.service.api.QueryApi;
import com.janbabak.noqlbackend.service.chat.ChatService;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class QueryServiceIntegrationTest extends PostgresTest {

    @Autowired
    private QueryService queryService;

    @Autowired
    private DatabaseEntityService databaseService;

    @Autowired
    private ChatService chatService;

    MockedStatic<LlmApiServiceFactory> apiServiceMock = Mockito.mockStatic(LlmApiServiceFactory.class);

    QueryApi queryApi = Mockito.mock(QueryApi.class);

    @Override
    protected String getCreateScript() {
        return FileUtils.getFileContent("./src/test/resources/dbInsertScripts/postgresUsers.sql");
    }

    @BeforeAll
    @Override
    protected void setUp() throws DatabaseConnectionException, DatabaseExecutionException {
        super.setUp();

        databaseService.create(postgresDatabase);

        apiServiceMock
                .when(() -> LlmApiServiceFactory.getQueryApiService(LlmModel.GPT_4o))
                .thenReturn(queryApi);
    }

    @AfterAll
    void tearDown() {
        apiServiceMock.close();
    }

    @Test
    @DisplayName("Test execute query language query")
    void testExecuteQueryLanguageQuery()
            throws DatabaseConnectionException, BadRequestException, EntityNotFoundException {

        // given
        UUID databaseId = postgresDatabase.getId();
        Integer page = 1;
        Integer pageSize = 5;
        // language=SQL
        String query = "SELECT id, name, age, sex, email FROM public.user ORDER BY name;";

        QueryResponse expectedResponse = new QueryResponse(
                new QueryResponse.RetrievedData(
                        List.of("id", "name", "age", "sex", "email"),
                        List.of(List.of("10", "David Taylor", "45", "M         ", "david.taylor@example.com"),
                                List.of("19", "Ella Thomas", "24", "F         ", "ella.thomas@example.com"),
                                List.of("5", "Emily Johnson", "40", "F         ", "emily.johnson@example.com"),
                                List.of("17", "Emma Scott", "30", "F         ", "emma.scott@example.com"),
                                List.of("21", "Grace Miller", "34", "F         ", "grace.miller@example.com"))),
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

    @Test
    @DisplayName("Test load chat results")
    void testLoadChatResults() throws EntityNotFoundException, DatabaseConnectionException, BadRequestException {
        // given
        UUID databaseId = postgresDatabase.getId();
        Integer page = 1;
        Integer pageSize = 10;
        ChatDto chat = chatService.create(databaseId);
        CreateChatQueryWithResponseRequest messageRequest1 = new CreateChatQueryWithResponseRequest(
                "find emails of all users",
                // language=JSON
                """
                        {
                            "databaseQuery": "SELECT email FROM public.user;",
                            "generatePlot": false,
                            "pythonCode": ""
                        }""");
        CreateChatQueryWithResponseRequest messageRequest2 = new CreateChatQueryWithResponseRequest(
                "sort them in descending order",
                // language=JSON
                """
                        {
                            "databaseQuery": "SELECT email FROM public.user ORDER BY email DESC;",
                            "generatePlot": false,
                            "pythonCode": ""
                        }""");

        chatService.addMessageToChat(chat.getId(), messageRequest1);
        ChatQueryWithResponse message2 = chatService.addMessageToChat(chat.getId(), messageRequest2);

        QueryResponse expectedResponse = new QueryResponse(
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
                        message2.getId(),
                        "sort them in descending order",
                        new ChatQueryWithResponseDto.LLMResult(
                                // language=SQL
                                "SELECT email FROM public.user ORDER BY email DESC;",
                                null),
                        message2.getTimestamp()),
                null);


        // when
        QueryResponse queryResponse = queryService.loadChatResult(databaseId, chat.getId(), page, pageSize);

        // then
        assertEquals(pageSize, queryResponse.getData().getRows().size()); // page size
        assertEquals(22, queryResponse.getTotalCount());
        assertEquals(expectedResponse, queryResponse);

        // cleanup
        chatService.deleteChatById(chat.getId());
    }

    @Test
    @DisplayName("Test execute chat")
    void testExecuteChat() throws EntityNotFoundException, LLMException, BadRequestException,
            DatabaseConnectionException, DatabaseExecutionException {
        // given
        UUID databaseId = postgresDatabase.getId();
        Integer pageSize = 8;
        ChatDto chat = chatService.create(databaseId);
        CreateChatQueryWithResponseRequest messageRequest1 = new CreateChatQueryWithResponseRequest(
                "find emails of all users",
                // language=JSON
                """
                        {
                            "databaseQuery": "SELECT email FROM public.user;",
                            "generatePlot": false,
                            "pythonCode": ""
                        }""");

        QueryRequest request = new QueryRequest(chat.getId(), "sort them in descending order", LlmModel.GPT_4o);

        chatService.addMessageToChat(chat.getId(), messageRequest1);

        // language=JSON
        String llmResponse = """
                {
                    "databaseQuery": "SELECT email FROM public.user ORDER BY email DESC;",
                    "generatePlot": false
                }""";

        QueryResponse expectedResponse = new QueryResponse(
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
                                "SELECT email FROM public.user ORDER BY email DESC;",
                                null),
                        null),
                null);

        // when
        when(queryApi.queryModel(any(), eq(request), any(), eq(new ArrayList<>()))).thenReturn(llmResponse);
        QueryResponse queryResponse = queryService.executeChat(databaseId, request, pageSize);

        // message id and timestamp are generated, so we need to set them manually
        expectedResponse.getChatQueryWithResponse().setId(
                queryResponse.getChatQueryWithResponse().getId());
        expectedResponse.getChatQueryWithResponse().setTimestamp(
                queryResponse.getChatQueryWithResponse().getTimestamp());

        // then
        assertEquals(pageSize, queryResponse.getData().getRows().size()); // page size
        assertEquals(22, queryResponse.getTotalCount());
        assertEquals(expectedResponse, queryResponse);

        // cleanup
        chatService.deleteChatById(chat.getId());
    }

    @Test
    @DisplayName("Test execute chat with plot")
    void testExecuteChatWithPlot() throws EntityNotFoundException, DatabaseConnectionException,
            DatabaseExecutionException, LLMException, BadRequestException {
        // given
        UUID databaseId = postgresDatabase.getId();
        Integer pageSize = 8;
        ChatDto chat = chatService.create(databaseId);

        QueryRequest request = new QueryRequest(chat.getId(), "plot sex of users older than 24", LlmModel.GPT_4o);

        String llmResponse = FileUtils.getFileContent("./src/test/resources/llmResponses/plotSexOfUsersSuccess.json");

        QueryResponse expectedResponse = new QueryResponse(
                new QueryResponse.RetrievedData(
                        List.of("sex", "count"),
                        List.of(List.of("M         ", "11"), List.of("F         ", "11"))),
                2L,
                new ChatQueryWithResponseDto(
                        null,
                        "plot sex of users older than 24",
                        new ChatQueryWithResponseDto.LLMResult(
                                // language=SQL
                                "SELECT sex, COUNT(*) FROM public.user WHERE age > 4 GROUP BY sex",
                                "/static/images/" + chat.getId() + ".png"),
                        null),
                null);

        // when
        when(queryApi.queryModel(any(), eq(request), any(), eq(new ArrayList<>()))).thenReturn(llmResponse);
        // TODO: generates plot - consider mocking it
        QueryResponse queryResponse = queryService.executeChat(databaseId, request, pageSize);

        // message id and timestamp are generated, so we need to set them manually
        expectedResponse.getChatQueryWithResponse().setId(
                queryResponse.getChatQueryWithResponse().getId());
        expectedResponse.getChatQueryWithResponse().setTimestamp(
                queryResponse.getChatQueryWithResponse().getTimestamp());

        // then
        assertEquals(2, queryResponse.getData().getRows().size());
        assertEquals(2, queryResponse.getTotalCount());
        assertEquals(expectedResponse, queryResponse);

        // cleanup
        chatService.deleteChatById(chat.getId());
    }
}
