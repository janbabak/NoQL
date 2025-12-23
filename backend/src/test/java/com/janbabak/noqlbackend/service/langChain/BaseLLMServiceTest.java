package com.janbabak.noqlbackend.service.langChain;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BaseLLMServiceTest {

    @Autowired
    @SuppressWarnings("unused")
    private BaseLLMService llmService;

    @Test
    @DisplayName("Test get model by invalid model id")
    public void testGetModelInvalidModelId() {

        String unsupportedModelId = "unsupported-model";

        Exception exception = assertThrows(BadRequestException.class,
                () -> llmService.getModel(unsupportedModelId));

        String expectedMessage = "Unsupported model ID: " + unsupportedModelId;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @ParameterizedTest
    @DisplayName("Test get model by valid model id")
    @MethodSource("provideValidModelIds")
    public void testGetModelValidModelId(String modelId, Class<?> clazz) throws BadRequestException {
        var model = llmService.getModel(modelId);
        assertNotNull(model);
        assertInstanceOf(clazz, model);
    }

    static Object[][] provideValidModelIds() {
        return new Object[][]{
                {
                        "gpt-4o",
                        OpenAiChatModel.class
                },
                {
                        "gpt-5-mini",
                        OpenAiChatModel.class
                },
                {
                        "claude-haiku-4-5-20251001",
                        AnthropicChatModel.class
                },
        };
    }
}