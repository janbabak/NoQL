package com.janbabak.noqlcustommodel.publicApi;

import com.janbabak.noqlcustommodel.gptApi.GptResponse;
import lombok.Data;

import java.util.List;

@Data
public class ModelResponse {
    private final String model;
    private final Usage usage;
    private final List<Choice> choices;

    public ModelResponse(GptResponse gptResponse) {
        this.model = gptResponse.getModel();
        this.usage = new Usage(gptResponse.getUsage());
        this.choices = gptResponse.getChoices().stream().map(Choice::new).toList();
    }

    /**
     * Information in the response about GPT API usage
     */
    @Data
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;

        public Usage(GptResponse.Usage usage) {
            this.prompt_tokens = usage.getPrompt_tokens();
            this.completion_tokens = usage.getCompletion_tokens();
            this.total_tokens = usage.getTotal_tokens();
        }
    }

    /**
     * Choice object from the GPT response
     */
    @Data
    public static class Choice {
        private LlmMessage message;
        private String finish_reason;
        private int index;

        public Choice(GptResponse.Choice choice) {
            this.message = choice.getMessage();
            this.finish_reason = choice.getFinish_reason();
            this.index = choice.getIndex();
        }
    }
}
