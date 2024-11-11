package com.janbabak.noqlbackend.service.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LlmApiServiceFactory {

    private final GptApiService gptApiService;
    private final LlamaApiService llamaApiService;
    private final GeminiApiService geminiApiService;
    private final ClaudeApiService claudeApiService;
    private final CustomModelApiService customModelApiService;

    /**
     * Get query API service based on the model.
     *
     * @param model large language model to be used for the translation
     * @return correct query API service
     */
    public QueryApi getQueryApiService(String model) {
        return switch (model) {
            case "gpt-4o", "gpt-4", "gpt-4-turbo", "gpt-4-32k", "gpt-3.5-turbo" -> gptApiService;
            case "llama3-70b", "llama-13b-chat" -> llamaApiService;
            case "gemini-1.5-pro", "gemini-1.5-flash" -> geminiApiService;
            case "claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022" -> claudeApiService;
            default -> customModelApiService;
        };
    }
}
