package com.janbabak.noqlbackend.model.query;

import lombok.Getter;

/**
 * Large language model to be used for the translation.
 */
@Getter
public enum LlmModel {
    GPT_5_MINI("gpt-5-mini", "GPT 5 mini"),
    GPT_5_2("gpt-5.2", "GPT 5.2"),
    GPT_5_NANO("gpt-5-nano", "GPT 5 nano"),
    GPT_4o_MINI("gpt-4o-mini", "GPT 4o mini"),
    GPT_4o("gpt-4o", "GPT 4o"),
    CLAUDE_4_5_HAIKU("claude-haiku-4-5-20251001", "Claude 4.5 haiku");

    private final String model;
    private final String label;

    LlmModel(String model, String label) {
        this.model = model;
        this.label = label;
    }
}
