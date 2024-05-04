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
    private String NLQuery; // natural language query

    @NotBlank
    private String LLMResponse; // LLM response JSON
}
