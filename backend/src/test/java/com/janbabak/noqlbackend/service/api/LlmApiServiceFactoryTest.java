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
                        "gpt-4",
                        GptApiService.class
                },
                {
                        "gpt-4-turbo",
                        GptApiService.class
                },
                {
                        "gpt-4-32k",
                        GptApiService.class
                },
                {
                        "gpt-3.5-turbo",
                        GptApiService.class
                },
                {
                        "llama3-70b",
                        LlamaApiService.class
                },
                {
                        "llama-13b-chat",
                        LlamaApiService.class
                }
        };
    }
}