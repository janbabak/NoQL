package com.janbabak.noqlbackend.model.gpt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GptResponse {
    private String id;
    private String object;
    private String model;
    private Usage usage;
    private List<Choice> choices;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Choice {
        private GptQuery.Message message;
        private String finish_reason;
        private int index;
    }
}
