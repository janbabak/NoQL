package com.janbabak.noqlbackend.model.query.gpt;

import lombok.Getter;

/**
 * Large language model to be used for the translation.
 */
@Getter
public enum LlmModel {
    GPT_4o("gpt-4o", "GPT 4o"),
    GPT_4("gpt-4", "GPT 4"),
    GPT_4_TURBO("gpt-4-turbo", "GPT 4 turbo"),
    GPT_4_32K("gpt-4-32k", "GPT 4 32k"),
    GPT_3_5_TURBO("gpt-3.5-turbo", "GPT 3.5 turbo"),
    LLAMA3_70B("llama3-70b", "LLAMA3 70B"),
    LLAMA3_13B_CHAT("llama-13b-chat", "LLAMA3 13B chat"),
    GEMINI_1DOT5_PRO("gemini-1.5-pro", "Gemini 1.5 pro"),
    GEMINI_1DOT5_FLASH("gemini-1.5-flash", "Gemini 1.5 flash");

    private final String model;
    private final String label;

    LlmModel(String model, String label) {
        this.model = model;
        this.label = label;
    }
}
