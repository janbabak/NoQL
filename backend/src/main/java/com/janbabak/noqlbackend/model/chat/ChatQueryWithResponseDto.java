package com.janbabak.noqlbackend.model.chat;

import com.janbabak.noqlbackend.config.ResourceConfig;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.service.PlotService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ChatQueryWithResponseDto {
    private UUID id;
    private String nlQuery; // natural language query
    private LLMResult llmResult; // generated query in query language or plot
    private Timestamp timestamp;

    public ChatQueryWithResponseDto(ChatQueryWithResponse chatQueryWithResponse, LLMResult parsedLlmResult) {
        this(
                chatQueryWithResponse.getId(),
                chatQueryWithResponse.getNlQuery(),
                parsedLlmResult,
                chatQueryWithResponse.getTimestamp());
    }

    /**
     * Large language model result that contains database query and plot url.
     */
    @Data
    @AllArgsConstructor
    public static class LLMResult {
        private String databaseQuery; // if null, result contains just the plot without  a table
        private String plotUrl; // if null, plot wasn't generated

        /**
         * Create LLMResult from LLMResponse
         *
         * @param LLMResponse response of large language model
         * @param fileName    name of the file that contains the plot without extension
         */
        public LLMResult(LLMResponse LLMResponse, String fileName) {
            this.databaseQuery = LLMResponse.databaseQuery();
            this.plotUrl = LLMResponse.generatePlot()
                    ? ResourceConfig.IMAGES_STATIC_FOLDER + fileName + PlotService.PLOT_IMAGE_FILE_EXTENSION
                    : null;
        }
    }
}
