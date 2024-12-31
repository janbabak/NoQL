package com.janbabak.noqlbackend.model.query.gpt;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.LlmMessage;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GptRequestTest {
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
        GptRequest expected = new GptRequest(
                "llama3.1-70b",
                List.of(new LlmMessage(LlmMessage.Role.system, "system query"),
                        new LlmMessage(LlmMessage.Role.user, "query1"),
                        new LlmMessage(LlmMessage.Role.assistant, "response1"),
                        new LlmMessage(LlmMessage.Role.user, "query2"),
                        new LlmMessage(LlmMessage.Role.assistant, "response2"),
                        new LlmMessage(LlmMessage.Role.user, "query"),
                        new LlmMessage(LlmMessage.Role.system, "error1"),
                        new LlmMessage(LlmMessage.Role.system, "error2")));

        // when
        GptRequest actual = new GptRequest(chatHistory, queryRequest, systemQuery, errors);

        // then
        assertEquals(expected.model, actual.model);
        assertEquals(expected.messages, actual.messages);
    }
}