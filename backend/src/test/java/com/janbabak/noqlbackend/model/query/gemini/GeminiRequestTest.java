package com.janbabak.noqlbackend.model.query.gemini;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeminiRequestTest {

    @Test
    @DisplayName("Create claude request")
    public void createClaudeRequest() {
        // given
        QueryRequest queryRequest = new QueryRequest("query", "llama3.1-70b");
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
        GeminiRequest expected = new GeminiRequest(
                List.of(new GeminiMessage("user", List.of(new GeminiMessage.Part("system query"))),
                        new GeminiMessage("user", List.of(new GeminiMessage.Part("query1"))),
                        new GeminiMessage("assistant", List.of(new GeminiMessage.Part("response1"))),
                        new GeminiMessage("user", List.of(new GeminiMessage.Part("query2"))),
                        new GeminiMessage("assistant", List.of(new GeminiMessage.Part("response2"))),
                        new GeminiMessage("user", List.of(new GeminiMessage.Part("query"))),
                        new GeminiMessage("user", List.of(new GeminiMessage.Part("error1"))),
                        new GeminiMessage("user", List.of(new GeminiMessage.Part("error2")))));

        // when
        GeminiRequest actual = new GeminiRequest(chatHistory, queryRequest, systemQuery, errors);

        // then
        assertEquals(expected, actual);
    }
}