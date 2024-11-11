package com.janbabak.noqlbackend.model.query.claude;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.QueryRequest;

import java.util.ArrayList;
import java.util.List;

public record ClaudeRequest(
        String model,
        Integer max_tokens,
        String system,
        List<Message> messages) {


    /**
     * Create query
     *
     * @param chatHistory  chat history
     * @param queryRequest new natural language query, model, ...
     * @param systemQuery  instructions from the NoQL system about task that needs to be done
     * @param errors       list of errors from previous executions that should help the model fix its query
     */
    public ClaudeRequest(
            List<ChatQueryWithResponse> chatHistory,
            QueryRequest queryRequest,
            String systemQuery,
            List<String> errors
    ) {
        this(queryRequest.getModel(), 2048, systemQuery, new ArrayList<>());

        // chat history
        for (ChatQueryWithResponse chatQueryWithResponse : chatHistory) {
            this.messages().add(new Message("user", chatQueryWithResponse.getNlQuery()));
            this.messages().add(new Message("assistant", chatQueryWithResponse.getLlmResponse()));
        }

        // query
        this.messages().add(new Message("user", queryRequest.getQuery()));

        // system errors
        for (String error : errors) {
            this.messages().add(new Message("user", error));
        }

    }

    public record Message(
            String role,
            String content) {
    }
}
