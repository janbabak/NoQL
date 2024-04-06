package com.janbabak.noqlbackend.model.query.gpt;

import com.janbabak.noqlbackend.model.query.ChatRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * GPT query object which is sent to the GPT API.
 */
public class GptQuery {
    // available models
    public static final String GPT_3_5_TURBO = "gpt-3.5-turbo";
    @SuppressWarnings("unused")
    public static final String GPT_4 = "gpt-4";
    @SuppressWarnings("unused")
    public static final String GPT_4_32K = "gpt-4-32k";

    public String model; // GPT LLM
    public List<Message> messages; // list of messages - can contain user, system, and assistant messages

    /**
     * Create query
     * @param chatRequest contains user's messages with assistant's responses
     * @param systemQuery instructions from the NoQL system about task that needs to be done
     * @param model LLM to be used for the translation
     */
    public GptQuery(ChatRequest chatRequest, String systemQuery, String model) {
        this.model = model;

        this.messages = new ArrayList<>();
        this.messages.add(new Message(Role.system, systemQuery));

        for (int i = 0; i < chatRequest.getMessages().size(); i++) {
            this.messages.add(new Message(
                    i % 2 == 0 ? Role.user : Role.assistant,
                    chatRequest.getMessages().get(i)));
        }
    }

    @Data
    @AllArgsConstructor
    public static class Message {
        public Role role;
        public String content;
    }

    public enum Role {
        /** user, who is asking */
        user,
        /** system developer (me) who provides additional information */
        system,
        /** gpt LLM */
        assistant,
    }
}
