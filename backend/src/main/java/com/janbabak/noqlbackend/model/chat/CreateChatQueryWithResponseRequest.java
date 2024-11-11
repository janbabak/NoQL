package com.janbabak.noqlbackend.model.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * llm result is JSON in form of string<br />
 * {@code { databaseQuery: string, generatePlot: boolean, pythonCode: string }}
 */
@Builder
public record CreateChatQueryWithResponseRequest(
        @NotBlank
        String nlQuery, // natural language query

        @NotBlank
        String llmResult /* LLM response JSON */) {
}
