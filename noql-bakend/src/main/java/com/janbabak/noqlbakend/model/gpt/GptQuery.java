package com.janbabak.noqlbakend.model.gpt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class GptQuery {
    public static String GPT_3_5_TURBO = "gpt-3.5-turbo";

    @SuppressWarnings("unused")
    public static String GPT_4 = "gpt-4";
    @SuppressWarnings("unused")
    public static String GPT_4_32K = "gpt-4-32k";

    public String model;
    public List<Message> messages;

    public GptQuery(String query, String model) {
        this.model = model;
        this.messages = List.of(new Message("user", query));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        public String role;
        public String content;
    }
}
