package com.janbabak.noqlbackend.model.chat;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChatHistoryItem {
    private UUID id;
    private String name;
}
