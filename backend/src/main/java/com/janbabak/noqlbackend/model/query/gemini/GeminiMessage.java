package com.janbabak.noqlbackend.model.query.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GeminiMessage {

    public final Role role;
    public final List<Part> parts;

    public GeminiMessage(Role role, String message) {
        this.role = role;
        this.parts = List.of(new Part(message));
    }

    public record Part(String text) {}

    public enum Role {
        /**
         * user's query
         */
        user,

        system,
        /**
         * LLM response
         */
        assistant,
    }
}
