package com.janbabak.noqlbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * Request from the API.
 */
@Data
@AllArgsConstructor
public class QueryRequest {
    private UUID databaseId;
    private String query;
}
