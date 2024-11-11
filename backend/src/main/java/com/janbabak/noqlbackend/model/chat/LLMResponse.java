package com.janbabak.noqlbackend.model.chat;

public record LLMResponse(
        String databaseQuery,
        Boolean generatePlot,
        String pythonCode
) {
}
