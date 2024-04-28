package com.janbabak.noqlbackend.model.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.janbabak.noqlbackend.model.chat.ChatResponse;
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
    private ChatResponse response; // LLM response
    private Timestamp timestamp;

    public ChatQueryWithResponseDto(ChatQueryWithResponse chatQueryWithResponse) throws JsonProcessingException {
        this(
                chatQueryWithResponse.getId(),
                chatQueryWithResponse.getMessage(),
                JsonUtils.createChatResponse(chatQueryWithResponse.getResponse()),
                chatQueryWithResponse.getTimestamp());
    }
}
