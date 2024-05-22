package com.janbabak.noqlbackend.model.query.gpt;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * GPT query object which is sent to the GPT API.
 */
public class GptRequest {
    public final String model; // GPT LLM
    public final List<Message> messages; // list of messages - can contain user, system, and assistant messages

    /**
     * Create query
     *
     * @param chatHistory  chat history
     * @param queryRequest new natural language query, model, ...
     * @param systemQuery  instructions from the NoQL system about task that needs to be done
     * @param errors       list of errors from previous executions that should help the model fix its query
     */
    public GptRequest(
            List<ChatQueryWithResponse> chatHistory,
            QueryRequest queryRequest,
            String systemQuery,
            List<String> errors) {

        this.model = queryRequest.getModel().getModel();
        this.messages = new ArrayList<>();

        // system instructions
        this.messages.add(new Message(Role.system, systemQuery));

        for (ChatQueryWithResponse chatQueryWithResponse : chatHistory) {
            this.messages.add(new Message(Role.user, chatQueryWithResponse.getNlQuery()));
            this.messages.add(new Message(Role.assistant, chatQueryWithResponse.getLlmResponse()));
        }

        this.messages.add(new Message(Role.user, queryRequest.getQuery()));

        // system errors
        for (String error : errors) {
            this.messages.add(new Message(Role.system, error));
        }
    }

    @Data
    @AllArgsConstructor
    public static class Message {
        public Role role;
        public String content;
    }

    public enum Role {
        /**
         * user, who is asking
         */
        user,
        /**
         * system developer (me) who provides additional information
         */
        system,
        /**
         * gpt LLM
         */
        assistant,
    }
}
