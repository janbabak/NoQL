package com.janbabak.noqlbackend.model.chat;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
public class ChatDto {
    private UUID id;
    private String name;
    private Timestamp modificationDate;
}
