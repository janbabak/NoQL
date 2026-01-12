package com.janbabak.noqlbackend.model.query;

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
    private String description;
    private String dbExecutionErrorMessage;
    private String plotGenerationErrorMessage;
    private Timestamp timestamp;

    public ChatResponse(RetrievedData data, ChatQueryWithResponse chatQueryWithResponse, String plotUrl) {
        this(
                data,
                chatQueryWithResponse.getId(),
                chatQueryWithResponse.getNlQuery(),
                chatQueryWithResponse.getDbQuery(),
                plotUrl,
                chatQueryWithResponse.getResultDescription(),
                chatQueryWithResponse.getDbExecutionErrorMessage(),
                chatQueryWithResponse.getPlotGenerationErrorMessage(),
                chatQueryWithResponse.getTimestamp());
    }
    
    /**
     * Create response with error message and no data.
     *
     * @param error   error message
     * @param nlQuery natural language query
     * @return response
     */
    public static ChatResponse failedResponse(String dbExecutionErrorMessage, String plotGenerationErrorMessage, String nlQuery) {
        return new ChatResponse(
                null,
                null,
                nlQuery,
                null,
                null,
                null,
                dbExecutionErrorMessage,
                plotGenerationErrorMessage,
                null);
    }
}
