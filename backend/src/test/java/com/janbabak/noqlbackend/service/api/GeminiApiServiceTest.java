package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.model.query.QueryRequest;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GeminiApiServiceTest {

    private final GeminiApiService geminiApiService = new GeminiApiService();

    @ParameterizedTest
    @MethodSource("validRequestDataProvider")
    void testValidateSuccessfulRequest(QueryRequest request) {
        assertDoesNotThrow(() -> geminiApiService.validateRequest(request));
    }

    static QueryRequest[] validRequestDataProvider() {
        return new QueryRequest[]{
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", "gemini-1.5-pro"),
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", "gemini-1.5-flash")
        };
    }

    @ParameterizedTest
    @MethodSource("badRequestDataProvider")
    void testValidateBadRequest(QueryRequest request) {
        assertThrows(BadRequestException.class, () -> geminiApiService.validateRequest(request));
    }

    static QueryRequest[] badRequestDataProvider() {
        return new QueryRequest[]{
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", "gpt-4o"),
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", "llama-13b-chat"),
        };
    }
}