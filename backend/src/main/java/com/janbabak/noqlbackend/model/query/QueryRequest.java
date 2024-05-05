package com.janbabak.noqlbackend.model.query;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryRequest {

    private UUID chatId;

    @NotBlank
    private String query; // new query to be added to the chat
}
