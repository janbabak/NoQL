package com.janbabak.noqlbackend.model.chat;

import com.janbabak.noqlbackend.model.entity.MessageWithResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatDto {
    private UUID id;
    private String name;
    private List<MessageWithResponseDto> messages;
    private Timestamp modificationDate;
}
