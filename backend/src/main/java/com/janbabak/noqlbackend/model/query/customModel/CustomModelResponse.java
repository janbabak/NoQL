package com.janbabak.noqlbackend.model.query.customModel;

import com.janbabak.noqlbackend.model.query.LlmMessage;
import lombok.Data;

import java.util.List;

public record CustomModelResponse(
        String model,
        Usage usage,
        List<Choice> choices
) {
    public record Usage(
            int prompt_tokens,
            int completion_tokens,
            int total_tokens
    ) {}

    public record Choice(
            LlmMessage message,
            String finish_reason,
            int index
    ) {}
}
