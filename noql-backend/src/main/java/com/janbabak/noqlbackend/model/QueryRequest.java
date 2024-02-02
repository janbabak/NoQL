package com.janbabak.noqlbackend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * Request from the API.
 */
@Data
@AllArgsConstructor
public class QueryRequest {

    @NotNull
    private UUID databaseId;

    @NotBlank
    private String query;
}
