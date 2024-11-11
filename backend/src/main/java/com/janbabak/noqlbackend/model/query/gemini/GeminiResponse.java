package com.janbabak.noqlbackend.model.query.gemini;

import java.util.List;

/**
 * Gemini response object from the Gemini API
 */
public record GeminiResponse(
        List<Candidate> candidates,
        UsageMetadata usageMetadata,
        String modelVersion) {

    public record Candidate(
            GeminiMessage content,
            String finishReason,
            double avgLogprobs) {
    }

    public record UsageMetadata(
            int promptTokenCount,
            int candidatesTokenCount,
            int totalTokenCount) {
    }
}
