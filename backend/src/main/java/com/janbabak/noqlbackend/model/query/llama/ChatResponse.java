package com.janbabak.noqlbackend.model.query.llama;

import com.janbabak.noqlbackend.model.chat.LLMResponse;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.UUID;

@Builder
public record ChatResponse(
        ChatResponseData data,
        UUID messageId,
        String nlQuery, // natural language query
        String dbQuery, // database query
        String plotUrl,
        Timestamp timestamp,
        String error
) {

    /**
     * Create response with data.
     * @param data retrieved data from database
     * @param chatQueryWithResponse chat query with response stored in database
     * @param llmResponse response from large language model
     * @param plotFileName name of the file that contains the plot without extension if exist
     */
    public ChatResponse(ChatResponseData data,
                        ChatQueryWithResponse chatQueryWithResponse,
                        LLMResponse llmResponse, String plotFileName) {
        this(
                data,
                chatQueryWithResponse.getId(),
                chatQueryWithResponse.getNlQuery(),
                llmResponse.databaseQuery(),
                llmResponse.generatePlot() ? plotFileName : null,
                chatQueryWithResponse.getTimestamp(),
                null);
    }

    /**
     * Create response with error message and no data.
     *
     * @param error error message
     * @return response
     */
    public static ChatResponse failedResponse(String error) {
        return new ChatResponse(
                null, null, null, null, null, null, error);
    }
}
