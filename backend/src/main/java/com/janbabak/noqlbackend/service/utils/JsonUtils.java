package com.janbabak.noqlbackend.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.janbabak.noqlbackend.model.chat.LLMResponse;

public class JsonUtils {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static LLMResponse createLLMResponse(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, LLMResponse.class);
    }
}
