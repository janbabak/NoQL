package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.model.query.gpt.LlmModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class LlmApiServiceFactoryTest {

    @ParameterizedTest
    @MethodSource("testGetQueryApiServiceDataProvider")
    @DisplayName("Test get query API service based on the model.")
    void testGetQueryApiService(LlmModel model, Class<?> expected) {
        // when
        QueryApi queryApiService = LlmApiServiceFactory.getQueryApiService(model.toString());

        // then
        assertInstanceOf(expected, queryApiService);
    }

    static Object[][] testGetQueryApiServiceDataProvider() {
        return new Object[][]{
                {
                        LlmModel.GPT_4o,
                        GptApiService.class
                },
                {
                        LlmModel.GPT_4,
                        GptApiService.class
                },
                {
                        LlmModel.GPT_4_TURBO,
                        GptApiService.class
                },
                {
                        LlmModel.GPT_4_32K,
                        GptApiService.class
                },
                {
                        LlmModel.GPT_3_5_TURBO,
                        GptApiService.class
                },
                {
                        LlmModel.LLAMA3_70B,
                        LlamaApiService.class
                },
                {
                        LlmModel.LLAMA3_13B_CHAT,
                        LlamaApiService.class
                }
        };
    }
}