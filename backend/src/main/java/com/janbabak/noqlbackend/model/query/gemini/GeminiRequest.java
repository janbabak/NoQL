package com.janbabak.noqlbackend.model.query.gemini;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.QueryRequest;

import java.util.ArrayList;
import java.util.List;

public record GeminiRequest(
        List<GeminiMessage> contents) {

    /**
     * Create query
     *
     * @param chatHistory  chat history
     * @param queryRequest new natural language query, model, ...
     * @param systemQuery  instructions from the NoQL system about task that needs to be done
     * @param errors       list of errors from previous executions that should help the model fix its query
     */
    public GeminiRequest(
            List<ChatQueryWithResponse> chatHistory,
            QueryRequest queryRequest,
            String systemQuery,
            List<String> errors
    ) {
        this(new ArrayList<>());

        // system instructions
        this.contents.add(new GeminiMessage(GeminiMessage.Role.user, systemQuery));

        // chat history
        for (ChatQueryWithResponse chatQueryWithResponse : chatHistory) {
            this.contents.add(new GeminiMessage(GeminiMessage.Role.user, chatQueryWithResponse.getNlQuery()));
            this.contents.add(new GeminiMessage(GeminiMessage.Role.assistant, chatQueryWithResponse.toString()));
        }

        // query
        this.contents.add(new GeminiMessage(GeminiMessage.Role.user, queryRequest.getQuery()));

        // system errors
        for (String error : errors) {
            this.contents.add(new GeminiMessage(GeminiMessage.Role.user, error));
        }
    }
}
