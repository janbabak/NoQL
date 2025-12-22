package com.janbabak.noqlbackend.service.langChain;

import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static java.time.Duration.ofSeconds;

public class BaseLLMService {

    @SuppressWarnings("all") // value is never assigned
    @Value("${app.externalServices.openAiApi.apiKey}")
    private String openAiApiKey;

    private final static List<String> SUPPORTED_OPENAI_MODELS = List.of(
            "gpt-4o-mini",
            "gpt-4o",
            "gpt-5-mini",
            "gpt-5.2",
            "gpt-5-nano"
    );

    protected ChatModel getModel(String modelId) throws BadRequestException {
        if (SUPPORTED_OPENAI_MODELS.contains(modelId)) {
            return buildOpenAiModel(modelId);
        }


        throw new BadRequestException("Unsupported model ID: " + modelId);
    }

    // TODO: set max tokens

    private OpenAiChatModel buildOpenAiModel(String modelId) {
        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(modelId)
                .timeout(ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .httpClientBuilder(new SpringRestClientBuilderFactory().create())
                .build();
    }
}
