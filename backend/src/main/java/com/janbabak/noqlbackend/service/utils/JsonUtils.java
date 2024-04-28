package com.janbabak.noqlbackend.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.janbabak.noqlbackend.model.chat.ChatResponse;

public class JsonUtils {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static ChatResponse createChatResponse(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, ChatResponse .class);
    }
}
