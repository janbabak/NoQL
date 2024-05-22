package com.janbabak.noqlbackend.model.query.llama;

import com.janbabak.noqlbackend.model.query.LlmMessage;
import lombok.Data;

import java.util.List;

/**
 * Llama response object from the Llama API
 */
@Data
public class LlamaResponse {
    private int created;
    private String model;
    private Usage usage;
    private List<Choice> choices;

    /**
     * Information in the response about Llama API usage
     */
    @Data
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }

    /**
     * Choice object from the Llama response
     */
    @Data
    public static class Choice {
        private LlmMessage message;
        private String finish_reason;
        private int index;

    }
}
