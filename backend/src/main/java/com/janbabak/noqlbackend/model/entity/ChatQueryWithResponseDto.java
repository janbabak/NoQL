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
//    private String response; // LLM response
    private Object response; // LLM response
    private Timestamp timestamp;

    public ChatQueryWithResponseDto(ChatQueryWithResponse chatQueryWithResponse) {
        this(
                chatQueryWithResponse.getId(),
                chatQueryWithResponse.getMessage(),
                chatQueryWithResponse.getResponse(),
                chatQueryWithResponse.getTimestamp());
    }
}
