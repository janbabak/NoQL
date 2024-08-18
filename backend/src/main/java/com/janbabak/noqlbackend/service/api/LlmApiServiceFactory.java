package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.model.query.gpt.LlmModel;

public class LlmApiServiceFactory {

    /**
     * Get query API service based on the model.
     *
     * @param model large language model to be used for the translation
     * @return correct query API service
     */
    @SuppressWarnings("all") // default branch is unnecessary
    public static QueryApi getQueryApiService(String model) {
//        try {
//            return switch (model)) {
//                case GPT_4o, GPT_4, GPT_4_TURBO, GPT_4_32K, GPT_3_5_TURBO -> new GptApiService();
//                case LLAMA3_70B, LLAMA3_13B_CHAT -> new LlamaApiService();
//            };
//        } catch (IllegalArgumentException e) {
//            return new CustomModelApiService();
//        }

        return switch (model) {
            case "gpt-4o", "gpt-4", "gpt-4-turbo", "gpt-4-32k", "gpt-3.5-turbo" -> new GptApiService();
            case "llama3-70b", "llama-13b-chat" -> new LlamaApiService();
            default -> new CustomModelApiService();
        };
    }
}
