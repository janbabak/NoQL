package com.janbabak.noqlbackend.model.query.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Gemini response object from the Gemini API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeminiResponse {

    public List<Candidate> candidates;
    public UsageMetadata usageMetadata;
    public String modelVersion;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Candidate {
        public GeminiMessage content;
        public String finishReason;
        public String avgLogprobs;  // TODO: double
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UsageMetadata {
        public int promptTokenCount;
        public int candidatesTokenCount;
        public int totalTokenCount;
    }

}
