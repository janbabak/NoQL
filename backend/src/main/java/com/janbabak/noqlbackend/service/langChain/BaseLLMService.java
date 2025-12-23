package com.janbabak.noqlbackend.service.langChain;

import com.janbabak.noqlbackend.config.llm.AnthropiConfig;
import com.janbabak.noqlbackend.config.llm.GeminiConfig;
import com.janbabak.noqlbackend.config.llm.OpenAiConfig;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

@Slf4j
public class BaseLLMService {

    @Autowired
    @SuppressWarnings("all")
    private OpenAiConfig openAiConfig;

    @Autowired
    @SuppressWarnings("all")
    private GeminiConfig geminiConfig;

    @Autowired
    @SuppressWarnings("all")
    private AnthropiConfig anthropicConfig;

    private final Duration TIMEOUT = ofSeconds(60);
    private final int MAX_TOKENS = 4096;
    private final HttpClientBuilder httpClientBuilder = new SpringRestClientBuilderFactory().create();

    /**
     * Get ChatModel instance based on the provided model ID.
     *
     * @param modelId the identifier of the model
     * @return ChatModel instance
     * @throws BadRequestException if the model ID is unsupported
     */
    protected ChatModel getModel(String modelId) throws BadRequestException {
        if (openAiConfig.getSupportedModels().contains(modelId)) {
            return buildOpenAiModel(modelId);
        }
        if (anthropicConfig.getSupportedModels().contains(modelId)) {
            return buildAnthropicModel(modelId);
        }
        if (geminiConfig.getSupportedModels().contains(modelId)) {
            return buildGoogleGeminiModel(modelId);
        }

        String errorMsg = "Unsupported model ID: " + modelId;
        log.error(errorMsg);
        throw new BadRequestException(errorMsg);
    }

    private OpenAiChatModel buildOpenAiModel(String modelId) {
        return OpenAiChatModel.builder()
                .apiKey(openAiConfig.getApiKey())
                .modelName(modelId)
                .timeout(TIMEOUT)
                .maxTokens(MAX_TOKENS)
                .logRequests(true)
                .logResponses(true)
                .httpClientBuilder(httpClientBuilder)
                .build();
    }

    // so far langchain4j doesn't support function tools in gemini API
    private GoogleAiGeminiChatModel buildGoogleGeminiModel(String modelId) {
        return GoogleAiGeminiChatModel.builder()
                .modelName(modelId)
                .apiKey(geminiConfig.getApiKey())
                .timeout(TIMEOUT)
                .maxOutputTokens(MAX_TOKENS)
                .allowCodeExecution(true)
                .httpClientBuilder(httpClientBuilder)
                .build();
    }

    private AnthropicChatModel buildAnthropicModel(String modelId) {
        return AnthropicChatModel.builder()
                .apiKey(anthropicConfig.getApiKey())
                .modelName(modelId)
                .timeout(TIMEOUT)
                .maxTokens(MAX_TOKENS)
                .logRequests(true)
                .logResponses(true)
                .httpClientBuilder(httpClientBuilder)
                .build();
    }
}
