package com.janbabak.noqlbackend.model.query.gpt;

import lombok.Getter;

/**
 * Large language model to be used for the translation.
 */
@Getter
public enum LlmModel {
    GPT_4o("gpt-4o", "GPT 4o"),
    GPT_4o_MINI("gpt-4o-mini", "GPT 4o mini"),
    GPT_4_TURBO("gpt-4-turbo", "GPT 4 turbo"),
    GEMINI_1DOT5_PRO("gemini-1.5-pro", "Gemini 1.5 pro"),
    GEMINI_1DOT5_FLASH("gemini-1.5-flash", "Gemini 1.5 flash"),
    CLAUDE_3_5_HAIKU_20241022("claude-3-5-haiku-20241022", "Claude 3.5 haiku"),
    LLAMA3DOT1_70B("llama3.1-70b", "LLAMA 3.1 70B");

    private final String model;
    private final String label;

    LlmModel(String model, String label) {
        this.model = model;
        this.label = label;
    }

    public static LlmModel fromModel(String model) {
        for (LlmModel value : values()) {
            if (value.model.equals(model)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Model " + model + " not found.");
    }
}
