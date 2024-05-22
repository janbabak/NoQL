package com.janbabak.noqlbackend.model.query.gpt;

import lombok.Getter;

/**
 * Large language model to be used for the translation.
 */
@Getter
public enum LlmModel {
    GPT_4o("gpt-4o"),
    GPT_4("gpt-4"),
    GPT_4_TURBO("gpt-4-turbo"),
    GPT_4_32K("gpt-4-32k"),
    GPT_3_5_TURBO("gpt-3.5-turbo"),
    LLAMA3_70B("llama3-70b"),
    LLAMA3_13B_CHAT("llama-13b-chat");

    private final String model;

    LlmModel(String model) {
        this.model = model;
    }
}
