package com.janbabak.noqlbackend.model.query.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeminiMessage {

    public String role;
    public List<Part> parts;

    public GeminiMessage(Role role, String message) {
        this.role = role.name();
        this.parts = List.of(new Part(message));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Part {
        public String text;
    }

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
