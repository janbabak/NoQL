package com.janbabak.noqlbackend.model.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {

    /**
     * List of messages, where the first message is use's query, second message is LLM response to that query
     * and so on... (even indices contain user's queries and odd indices contain LLM responses)
     */
    @NotEmpty
    List<@NotBlank String> messages;
}
