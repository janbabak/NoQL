package com.janbabak.noqlbackend.model.query;

import com.janbabak.noqlbackend.model.query.gpt.LlmModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryRequest {

    @NotNull
    private UUID chatId;

    @NotBlank
    private String query; // new query to be added to the chat

    @NotNull
    private String model; // model to be used for the translation, e.g. GPT-4 or id of custom model
}
