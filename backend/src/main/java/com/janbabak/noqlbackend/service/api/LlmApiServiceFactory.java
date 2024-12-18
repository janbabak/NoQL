package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.model.query.gpt.LlmModel;
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
        LlmModel llmModel = null;
        try {
            llmModel = LlmModel.fromModel(model);
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        if (llmModel == null) {
            return customModelApiService;
        }
        return switch (llmModel) {
            case GPT_4o, GPT_4o_MINI, GPT_4_TURBO -> gptApiService;
            case GEMINI_1DOT5_PRO, GEMINI_1DOT5_FLASH -> geminiApiService;
            case CLAUDE_3_5_HAIKU_20241022 -> claudeApiService;
            case LLAMA3DOT1_70B -> llamaApiService;
        };
    }
}
