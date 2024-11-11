package com.janbabak.noqlbackend.model.chat;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ChatHistoryItem(
        UUID id,
        String name) {
}
