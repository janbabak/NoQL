package com.janbabak.noqlcustommodel.publicApi;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LlmMessage {
    public Role role;
    public String content;

    public enum Role {
        /**
         * user's query
         */
        user,
        /**
         * system developer's additional information
         */
        system,
        /**
         * LLM response
         */
        assistant,
    }
}
