package com.janbabak.noqlbackend.model.query.gpt;

import com.janbabak.noqlbackend.model.query.LlmMessage;
import lombok.Data;

import java.util.List;

/**
 * GPT response object from the GPT API
 */
public record GptResponse(
        String id,
        String object,
        String model,
        Usage usage,
        List<Choice> choices) {

    /**
     * Information in the response about GPT API usage
     */
    public record Usage(
            int prompt_tokens,
            int completion_tokens,
            int total_tokens) {
    }

    /**
     * Choice object from the GPT response
     */
    public record Choice(
            LlmMessage message,
            String finish_reason,
            int index) {
    }
}
