package com.janbabak.noqlbackend.model.query.llama;

import java.sql.Timestamp;
import java.util.UUID;

public record ChatResponse(
        ChatResponseData data,
        UUID messageId,
        String nlQuery, // natural language query
        String dbQuery, // database query
        String plotUrl,
        Timestamp timestamp
) {
}
