package com.janbabak.noqlbackend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ChatQueryWithResponseDto {
    private UUID id;
    private String query; // query from user
    private ChatResponseResult chatResponseResult; // generated query in query language or plot
    private Timestamp timestamp;

    public ChatQueryWithResponseDto(ChatQueryWithResponse chatQueryWithResponse, ChatResponseResult parsedResponseResult) {
        this(
                chatQueryWithResponse.getId(),
                chatQueryWithResponse.getMessage(),
                parsedResponseResult,
                chatQueryWithResponse.getTimestamp());
    }

    @Data
    @AllArgsConstructor
    public static class ChatResponseResult {
        private String databaseQuery; // if null, result contains just the plot without  a table
        private String plotUrl; // if null, plot wasn't generated
    }
}
