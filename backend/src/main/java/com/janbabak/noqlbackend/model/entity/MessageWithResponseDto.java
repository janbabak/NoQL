package com.janbabak.noqlbackend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
public class MessageWithResponseDto {
    private UUID id;
    private String message;
    private String response;
    private Timestamp timestamp;
}
