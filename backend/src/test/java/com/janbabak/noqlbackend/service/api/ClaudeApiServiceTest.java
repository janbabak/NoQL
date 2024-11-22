package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.model.query.QueryRequest;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class ClaudeApiServiceTest {

    private final ClaudeApiService claudeApiService = new ClaudeApiService();

    @ParameterizedTest
    @MethodSource("validRequestDataProvider")
    void testValidateSuccessfulRequest(QueryRequest request) {
        assertDoesNotThrow(() -> claudeApiService.validateRequest(request));
    }

    static QueryRequest[] validRequestDataProvider() {
        return new QueryRequest[]{
                new QueryRequest("Find the oldest user", "claude-3-5-sonnet-20241022"),
                new QueryRequest("Find the oldest user", "claude-3-5-haiku-20241022")
        };
    }

    @ParameterizedTest
    @MethodSource("badRequestDataProvider")
    void testValidateBadRequest(QueryRequest request) {
        assertThrows(BadRequestException.class, () -> claudeApiService.validateRequest(request));
    }

    static QueryRequest[] badRequestDataProvider() {
        return new QueryRequest[]{
                new QueryRequest("Find the oldest user", "gpt-4o"),
                new QueryRequest("Find the oldest user", "llama-13b-chat"),
        };
    }
}