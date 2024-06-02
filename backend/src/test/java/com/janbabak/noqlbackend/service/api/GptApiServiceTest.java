package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.model.query.gpt.LlmModel;
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
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", LlmModel.GPT_4o),
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", LlmModel.GPT_4),
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", LlmModel.GPT_4_TURBO),
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", LlmModel.GPT_4_32K),
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", LlmModel.GPT_3_5_TURBO)
        };
    }

    @ParameterizedTest
    @MethodSource("badRequestDataProvider")
    void testValidateBadRequest(QueryRequest request) {
        assertThrows(BadRequestException.class, () -> gptApiService.validateRequest(request));
    }

    static QueryRequest[] badRequestDataProvider() {
        return new QueryRequest[]{
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", LlmModel.LLAMA3_70B),
                new QueryRequest(UUID.randomUUID(), "Find the oldest user", LlmModel.LLAMA3_13B_CHAT),
        };
    }
}