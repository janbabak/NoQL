package com.janbabak.noqlbackend.model.gpt;

import com.janbabak.noqlbackend.model.query.ChatRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.stream.IntStream;

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
    private final String GPT_USER_ROLE = "user";
    private final String GPT_SYSTEM_ROLE = "system";

    public String model;
    public List<Message> messages;

    public GptQuery(String query, String model) {
        this.model = model;
        this.messages = List.of(new Message("user", query));
    }

    public GptQuery(ChatRequest chatRequest, String model) {
        this.model = model;
        this.messages = IntStream.range(0, chatRequest.getMessages().size())
                .mapToObj(index -> new Message(
                        index % 2 == 0 ? GPT_USER_ROLE : GPT_SYSTEM_ROLE,
                        chatRequest.getMessages().get(index))
                )
                .toList();
    }

    @Data
    @AllArgsConstructor
    public static class Message {
        public String role;
        public String content;
    }
}
