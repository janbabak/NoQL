package com.janbabak.noqlbackend.model.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.janbabak.noqlbackend.service.utils.JsonUtils;
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

    // TODO
    public ChatQueryWithResponseDto(ChatQueryWithResponse chatQueryWithResponse, String plotUrl)
            throws JsonProcessingException {
        this(
                chatQueryWithResponse.getId(),
                chatQueryWithResponse.getMessage(),
                new ChatResponseResult(
                        JsonUtils.createChatResponse(chatQueryWithResponse.getResponse()).getDatabaseQuery(),
                        plotUrl),
                chatQueryWithResponse.getTimestamp());
    }

//    public ChatQueryWithResponseDto(ChatQueryWithResponse chatQueryWithResponse, ChatResponseResult responseResult) {
//        this(
//                chatQueryWithResponse.getId(),
//                chatQueryWithResponse.getMessage(),
//                responseResult,
//                chatQueryWithResponse.getTimestamp());
//    }

    @Data
    @AllArgsConstructor
    public static class ChatResponseResult {
        private String databaseQuery; // if null, result contains just the plot without  a table
        private String plotUrl; // if null, plot wasn't generated
    }
}
