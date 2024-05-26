package com.janbabak.noqlbackend.model.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatQueryWithResponseRequest {

    @NotBlank
    private String nlQuery; // natural language query

    /** JSON in form of string<br />
     * {@code { databaseQuery: string, generatePlot: boolean, pythonCode: string }}
     */
    @NotBlank
    private String llmResult; // LLM response JSON
}
