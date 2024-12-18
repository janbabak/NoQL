package com.janbabak.noqlbackend.service.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class LlmApiServiceFactoryTest {

    @Autowired
    private LlmApiServiceFactory llmApiServiceFactory;

    @ParameterizedTest
    @MethodSource("testGetQueryApiServiceDataProvider")
    @DisplayName("Test get query API service based on the model.")
    void testGetQueryApiService(String model, Class<?> expected) {
        // when
        QueryApi queryApiService = llmApiServiceFactory.getQueryApiService(model);

        // then
        assertInstanceOf(expected, queryApiService);
    }

    static Object[][] testGetQueryApiServiceDataProvider() {
        return new Object[][]{
                {
                        "gpt-4o",
                        GptApiService.class
                },
                {
                        "gpt-4o-mini",
                        GptApiService.class
                },
                {
                        "gpt-4-turbo",
                        GptApiService.class
                },
                {
                        "gemini-1.5-pro",
                        GeminiApiService.class
                },
                {
                        "gemini-1.5-flash",
                        GeminiApiService.class
                },
                {
                        "claude-3-5-haiku-20241022",
                        ClaudeApiService.class
                },
                {
                        "llama3.1-70b",
                        LlamaApiService.class
                },
                {
                        "custom-model",
                        CustomModelApiService.class
                },
        };
    }
}