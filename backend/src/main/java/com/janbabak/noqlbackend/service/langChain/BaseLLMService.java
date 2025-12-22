package com.janbabak.noqlbackend.service.langChain;

import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;

import static java.time.Duration.ofSeconds;

public class BaseLLMService {

    @SuppressWarnings("all") // value is never assigned
    @Value("${app.externalServices.openAiApi.apiKey}")
    private String openAiApiKey;

    protected ChatModel getModel(String modelId) {
        // TODO: implement model selection based on modelId

        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.0)
                .timeout(ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .httpClientBuilder(new SpringRestClientBuilderFactory().create())
                .build();
    }
}
