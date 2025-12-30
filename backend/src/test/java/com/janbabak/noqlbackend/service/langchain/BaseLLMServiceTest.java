package com.janbabak.noqlbackend.service.langchain;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BaseLLMServiceTest {

    @Autowired
    private BaseLLMService llmService;

    @Test
    @DisplayName("Test get model by invalid model id")
    void testGetModelInvalidModelId() {

        final String unsupportedModelId = "unsupported-model";

        final Exception exception = assertThrows(BadRequestException.class,
                () -> llmService.getModel(unsupportedModelId));

        final String expectedMessage = "Unsupported model ID: " + unsupportedModelId;
        final String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @ParameterizedTest
    @DisplayName("Test get model by valid model id")
    @MethodSource("provideValidModelIds")
    void testGetModelValidModelId(String modelId, Class<?> clazz) throws BadRequestException {
        final ChatModel model = llmService.getModel(modelId);
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