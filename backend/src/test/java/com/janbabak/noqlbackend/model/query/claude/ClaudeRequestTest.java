package com.janbabak.noqlbackend.model.query.claude;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import org.junit.jupiter.api.DisplayName;
import java.util.List;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClaudeRequestTest {

    @Test
    @DisplayName("Create claude request")
    public void createClaudeRequest() {
        // given
        QueryRequest queryRequest = new QueryRequest("query", "claude-3-5-haiku-20241022");
        String systemQuery = "system query";
        List<String> errors = List.of("error1", "error2");
        List<ChatQueryWithResponse> chatHistory = List.of(
                ChatQueryWithResponse.builder()
                        .nlQuery("query1")
                        .llmResponse("response1")
                        .build(),
                ChatQueryWithResponse.builder()
                        .nlQuery("query2")
                        .llmResponse("response2")
                        .build());
        ClaudeRequest expected = new ClaudeRequest(
                "claude-3-5-haiku-20241022",
                2048,
                "system query",
                List.of(new ClaudeRequest.Message("user", "query1"),
                        new ClaudeRequest.Message("assistant", "response1"),
                        new ClaudeRequest.Message("user", "query2"),
                        new ClaudeRequest.Message("assistant", "response2"),
                        new ClaudeRequest.Message("user", "query"),
                        new ClaudeRequest.Message("user", "error1"),
                        new ClaudeRequest.Message("user", "error2")));

        // when
        ClaudeRequest actual = new ClaudeRequest(chatHistory, queryRequest, systemQuery, errors);

        // then
        assertEquals(expected, actual);
    }
}