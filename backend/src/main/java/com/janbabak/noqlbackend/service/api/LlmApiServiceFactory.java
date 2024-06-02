package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.model.query.gpt.LlmModel;

public class LlmApiServiceFactory {

    /**
     * Get query API service based on the model.
     * @param model large language model to be used for the translation
     * @return correct query API service
     */
    @SuppressWarnings("all") // default branch is unnecessary
    public static QueryApi getQueryApiService(LlmModel model) {
        return switch (model) {
            case GPT_4o, GPT_4, GPT_4_TURBO, GPT_4_32K, GPT_3_5_TURBO -> new GptApiService();
            case LLAMA3_70B, LLAMA3_13B_CHAT -> new LlamaApiService();
            default -> throw new IllegalArgumentException("Unknown model: " + model);
        };
    }
}
