package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.model.query.QueryRequest;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class LlamaApiServiceTest {

    private final LlamaApiService llamaApiService = new LlamaApiService();

    @ParameterizedTest
    @DisplayName("Validate successful request")
    @MethodSource("validRequestDataProvider")
    void testValidateSuccessfulRequest(QueryRequest request) {
        assertDoesNotThrow(() -> llamaApiService.validateRequest(request));
    }

    static QueryRequest[] validRequestDataProvider() {
        return new QueryRequest[]{
                new QueryRequest("Find the oldest user", "llama3.1-70b"),
                new QueryRequest("Find the oldest user", "llama-13b-chat"),
        };
    }

    @ParameterizedTest
    @DisplayName("Validate bad request")
    @MethodSource("badRequestDataProvider")
    void testValidateBadRequest(QueryRequest request) {
        assertThrows(BadRequestException.class, () -> llamaApiService.validateRequest(request));
    }

    static QueryRequest[] badRequestDataProvider() {
        return new QueryRequest[]{
                new QueryRequest("Find the oldest user", "gpt-4o"),
                new QueryRequest("Find the oldest user", "gpt-4"),
                new QueryRequest("Find the oldest user", "gpt-4-turbo"),
                new QueryRequest("Find the oldest user", "gpt-4-32k"),
                new QueryRequest("Find the oldest user", "gpt-3.5-turbo")
        };
    }
}