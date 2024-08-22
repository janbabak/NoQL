package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.model.query.QueryRequest;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GptApiServiceTest {

    private final GptApiService gptApiService = new GptApiService();

    @ParameterizedTest
    @MethodSource("validRequestDataProvider")
    void testValidateSuccessfulRequest(QueryRequest request) {
        assertDoesNotThrow(() -> gptApiService.validateRequest(request));
    }

    static QueryRequest[] validRequestDataProvider() {
        return new QueryRequest[]{
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", "gpt-4o"),
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", "gpt-4"),
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", "gpt-4-turbo"),
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", "gpt-4-32k"),
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", "gpt-3.5-turbo")
        };
    }

    @ParameterizedTest
    @MethodSource("badRequestDataProvider")
    void testValidateBadRequest(QueryRequest request) {
        assertThrows(BadRequestException.class, () -> gptApiService.validateRequest(request));
    }

    static QueryRequest[] badRequestDataProvider() {
        return new QueryRequest[]{
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", "llama3-70b"),
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", "llama-13b-chat"),
        };
    }
}