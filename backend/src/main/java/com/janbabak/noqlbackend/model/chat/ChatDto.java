package com.janbabak.noqlbackend.model.chat;

import com.janbabak.noqlbackend.model.query.ChatResponse;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Builder
public record ChatDto(
        UUID id,
        String name,
        List<ChatResponse> messages,
        Timestamp modificationDate,
        UUID databaseId) {
}
