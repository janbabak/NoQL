package com.janbabak.noqlbackend.model.query.gemini;

import lombok.Data;

import java.util.List;

@Data
public class GeminiResponse {

    private final List<Candidate> candidateList;

    public record Candidate(GeminiMessage content, String finishReason) {}

}
