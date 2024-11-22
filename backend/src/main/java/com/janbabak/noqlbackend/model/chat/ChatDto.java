package com.janbabak.noqlbackend.model.chat;

import lombok.Builder;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Builder
public record ChatDto(
        UUID id,
        String name,
        List<ChatQueryWithResponseDto> messages,
        Timestamp modificationDate,
        UUID databaseId) {
}
