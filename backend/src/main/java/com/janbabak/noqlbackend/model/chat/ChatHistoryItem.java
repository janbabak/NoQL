package com.janbabak.noqlbackend.model.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ChatHistoryItem {
    private UUID id; // TODO: rename to chatId?
    private String name;
}
