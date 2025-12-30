package com.janbabak.noqlbackend.model.query;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import jakarta.annotation.Nullable;
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
    private Timestamp timestamp;
    private String error;

    public ChatResponse(RetrievedData data, ChatQueryWithResponse chatQueryWithResponse, String plotUrl) {
        this(
                data,
                chatQueryWithResponse.getId(),
                chatQueryWithResponse.getNlQuery(),
                chatQueryWithResponse.getDbQuery(),
                plotUrl,
                chatQueryWithResponse.getResultDescription(),
                chatQueryWithResponse.getTimestamp(),
                null);

        final String error = getError(chatQueryWithResponse);
        if (error != null) {
            this.error = error;
        }
    }

    @Nullable
    private static String getError(ChatQueryWithResponse chatQueryWithResponse) {
        String error = null;
        if (chatQueryWithResponse.getDbExecutionErrorMessage() != null) {
            error = "Errors: \n" + chatQueryWithResponse.getDbExecutionErrorMessage();
        }
        if (chatQueryWithResponse.getPlotGenerationErrorMessage() != null) {
            if (error == null) {
                error = "Errors: \n" + chatQueryWithResponse.getPlotGenerationErrorMessage();
            } else {
                error += "\n" + chatQueryWithResponse.getPlotGenerationErrorMessage();
            }
        }
        return error;
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
                null, null, nlQuery, null, null, null, null, error);
    }
}
