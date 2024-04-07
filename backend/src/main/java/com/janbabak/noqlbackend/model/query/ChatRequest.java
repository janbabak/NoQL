package com.janbabak.noqlbackend.model.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest { // TODO: rename to query request

    private UUID chatId;
    private String message; // new message to be added to the chat
}
