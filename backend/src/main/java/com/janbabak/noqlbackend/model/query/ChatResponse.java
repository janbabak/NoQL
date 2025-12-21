package com.janbabak.noqlbackend.model.query;

import com.janbabak.noqlbackend.model.chat.LLMResponse;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ChatResponse {
    private RetrievedData data;
    private UUID messageId;
    private String nlQuery; // natural language query
    private String dbQuery; // database query
    private String plotUrl;
    private Timestamp timestamp;
    private String error;

    /**
     * Create response with data.
     *
     * @param data                  retrieved data from database
     * @param chatQueryWithResponse chat query with response stored in database
     * @param llmResponse           response from large language model
     * @param plotFileName          name of the file that contains the plot without extension if exist
     */
    public ChatResponse(RetrievedData data,
                        ChatQueryWithResponse chatQueryWithResponse,
                        LLMResponse llmResponse,
                        String plotFileName) {
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
     * @param error   error message
     * @param nlQuery natural language query
     * @return response
     */
    public static ChatResponse failedResponse(String error, String nlQuery) {
        return new ChatResponse(
                null, null, nlQuery, null, null, null, error);
    }
}
