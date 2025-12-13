package com.janbabak.noqlbackend.service.langChain;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static java.time.Duration.ofSeconds;

@Slf4j
@Service
public class LLMService {

    @SuppressWarnings("all") // value is never assigned
    @Value("${app.externalServices.openAiApi.apiKey}")
    private String openAiApiKey;

    public String runQuery(String query) {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.0)
                .timeout(ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        Assistant assistant = AiServices.create(Assistant.class, model);

        String response = assistant.chat(query);

        if (response == null || response.isEmpty()) {
            log.error("LLM returned empty response for query: {}", query);
            return null;
        }

        log.info("LLM response: {}", response);
        return response;
    }
}
