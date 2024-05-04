package com.janbabak.noqlbackend.model.entity;

import com.janbabak.noqlbackend.config.ResourceConfig;
import com.janbabak.noqlbackend.model.chat.LLMResponse;
import com.janbabak.noqlbackend.service.PlotService;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ChatQueryWithResponseDto {
    private UUID id;
    private String NLQuery; // natural language query
    private ChatResponseResult chatResponseResult; // generated query in query language or plot
    private Timestamp timestamp;

    public ChatQueryWithResponseDto(ChatQueryWithResponse chatQueryWithResponse, ChatResponseResult parsedResponseResult) {
        this(
                chatQueryWithResponse.getId(),
                chatQueryWithResponse.getNLQuery(),
                parsedResponseResult,
                chatQueryWithResponse.getTimestamp());
    }

    @Data
    @AllArgsConstructor
    public static class ChatResponseResult {
        private String databaseQuery; // if null, result contains just the plot without  a table
        private String plotUrl; // if null, plot wasn't generated

        public ChatResponseResult(LLMResponse LLMResponse, UUID chatId) {
            this.databaseQuery = LLMResponse.getDatabaseQuery();
            this.plotUrl = LLMResponse.getGeneratePlot()
                    ? ResourceConfig.IMAGES_STATIC_FOLDER + chatId + PlotService.PLOT_IMAGE_FILE_EXTENSION
                    : null;
        }
    }
}
