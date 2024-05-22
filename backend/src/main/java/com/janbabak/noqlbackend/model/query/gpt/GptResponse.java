package com.janbabak.noqlbackend.model.query.gpt;

import com.janbabak.noqlbackend.model.query.LlmMessage;
import lombok.Data;

import java.util.List;

/**
 * GPT response object from the GPT API
 */
@Data
public class GptResponse {
    private String id;
    private String object;
    private String model;
    private Usage usage;
    private List<Choice> choices;

    /**
     * Information in the response about GPT API usage
     */
    @Data
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }

    /**
     * Choice object from the GPT response
     */
    @Data
    public static class Choice {
        private LlmMessage message;
        private String finish_reason;
        private int index;
    }
}
