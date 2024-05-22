package com.janbabak.noqlbackend.model.query.llama;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.LlmMessage;
import com.janbabak.noqlbackend.model.query.LlmMessage.Role;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class LlamaQuery {
    // available models
    @SuppressWarnings("unused")
    public static final String LLAMA3_70B = "llama3-70b";
    @SuppressWarnings("unused")
    public static final String LLAMA3_13B_CHAT = "llama-13b-chat";

    public String model; // LLAMA LLM

    public List<LlmMessage> messages;

    /**
     * Create query
     *
     * @param chatHistory chat history
     * @param query       new natural language query
     * @param systemQuery instructions from the NoQL system about task that needs to be done
     * @param errors      list of errors from previous executions that should help the model fix its query
     * @param model       LLM to be used for the translation
     */
    public LlamaQuery(
            List<ChatQueryWithResponse> chatHistory,
            String query,
            String systemQuery,
            List<String> errors,
            String model) {

        this.model = model;
        this.messages = new ArrayList<>();

        this.messages.add(new LlmMessage(Role.system, systemQuery));

        for (ChatQueryWithResponse chatQueryWithResponse : chatHistory) {
            this.messages.add(new LlmMessage(Role.user, chatQueryWithResponse.getNlQuery()));
            this.messages.add(new LlmMessage(Role.assistant, chatQueryWithResponse.getLlmResponse()));
        }

        this.messages.add(new LlmMessage(Role.user, query));

        // system errors
        for (String error : errors) {
            this.messages.add(new LlmMessage(Role.system, error));
        }
    }
}
