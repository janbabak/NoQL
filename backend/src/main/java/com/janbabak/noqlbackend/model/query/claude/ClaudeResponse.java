package com.janbabak.noqlbackend.model.query.claude;

import java.util.List;

public record ClaudeResponse(
        String id,
        String type,
        String model,
        List<Content> content,
        String stop_reason,
        String stop_sequence,
        Usage usage
) {

    public record Content(
            String type,
            String text
    ) {
    }

    public record Usage(
            int input_tokens,
            int output_tokens) {
    }
}
