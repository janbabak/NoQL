package com.janbabak.noqlbackend.model.query.gpt;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.LlmMessage;
import com.janbabak.noqlbackend.model.query.LlmMessage.Role;
import com.janbabak.noqlbackend.model.query.QueryRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * GPT request object which is sent to the GPT API.
 */
public class GptRequest {
    public final String model; // GPT LLM
    public final List<LlmMessage> messages; // list of messages - can contain user, system, and assistant messages

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
        this.messages.add(new LlmMessage(Role.system, systemQuery));

        for (ChatQueryWithResponse chatQueryWithResponse : chatHistory) {
            this.messages.add(new LlmMessage(Role.user, chatQueryWithResponse.getNlQuery()));
            this.messages.add(new LlmMessage(Role.assistant, chatQueryWithResponse.getLlmResponse()));
        }

        this.messages.add(new LlmMessage(Role.user, queryRequest.getQuery()));

        // system errors
        for (String error : errors) {
            this.messages.add(new LlmMessage(Role.system, error));
        }
    }
}
