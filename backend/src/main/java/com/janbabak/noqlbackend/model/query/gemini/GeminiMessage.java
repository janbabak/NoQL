package com.janbabak.noqlbackend.model.query.gemini;

import java.util.List;

public record GeminiMessage(
        String role,
        List<Part> parts) {

    public GeminiMessage(Role role, String message) {
        this(role.name(), List.of(new Part(message)));
    }

    public record Part(String text) {}

    public enum Role {
        /**
         * user's query
         */
        user,

        /**
         * LLM response
         */
        assistant,
    }
}
