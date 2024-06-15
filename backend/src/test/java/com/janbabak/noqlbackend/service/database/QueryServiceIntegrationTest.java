package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.PostgresTest;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.ChatQueryWithResponseDto;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.QueryResponse;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.chat.ChatService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class QueryServiceIntegrationTest extends PostgresTest {

    @Autowired
    private QueryService queryService;

    @Autowired
    private DatabaseEntityService databaseService;

    @Autowired
    private ChatService chatService;

    @Override
    protected String getCreateScript() {
        return loadScriptFromFile("./src/test/resources/dbInsertScripts/postgresUsers.sql");
    }

    @BeforeAll
    @Override
    protected void setUp() throws DatabaseConnectionException, DatabaseExecutionException {
        super.setUp();
        databaseService.create(postgresDatabase);
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
    }

}
