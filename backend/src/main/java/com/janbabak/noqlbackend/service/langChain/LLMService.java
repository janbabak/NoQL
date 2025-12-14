package com.janbabak.noqlbackend.service.langChain;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static java.time.Duration.ofSeconds;

@Slf4j
@Service
@RequiredArgsConstructor
public class LLMService {

    @SuppressWarnings("all") // value is never assigned
    @Value("${app.externalServices.openAiApi.apiKey}")
    private String openAiApiKey;

    private final AssistantTools assistantTools;

    public AssistantTools.QueryResult runQuery(String query) {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.0)
                .timeout(ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .systemMessageProvider(o -> "Your role is to identify what user wants from the natural language query and call the appropriate tools to fulfill the request.")
                .tools(assistantTools)
                .build();

        String response = assistant.chat(query);

        log.info("LLM raw response: {}", response);

        if (response == null) {
            log.error("LLM returned empty response for query: {}", query);
            return null;
        }

        log.info("LLM response: {}", response);

        AssistantTools.QueryResult toolResult = assistantTools.getResult();
        log.info("LLM tool result: {}", toolResult != null ? toolResult.getMessage() : "no tool used");

        return toolResult;
    }
}
