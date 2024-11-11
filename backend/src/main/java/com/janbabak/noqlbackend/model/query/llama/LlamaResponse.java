package com.janbabak.noqlbackend.model.query.llama;

import com.janbabak.noqlbackend.model.query.LlmMessage;

import java.util.List;

/**
 * Llama response object from the Llama API
 */
public record LlamaResponse(
        int created,
        String model,
        Usage usage,
        List<Choice> choices) {

    /**
     * Information in the response about Llama API usage
     */
    public record Usage(
            int prompt_tokens,
            int completion_tokens,
            int total_tokens) {
    }

    /**
     * Choice object from the Llama response
     */
    public record Choice(
            LlmMessage message,
            String finish_reason,
            int index) {
    }
}
