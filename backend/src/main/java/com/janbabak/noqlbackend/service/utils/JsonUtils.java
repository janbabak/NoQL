package com.janbabak.noqlbackend.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.janbabak.noqlbackend.model.chat.LLMResponse;

public class JsonUtils {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convert LLM response from JSON string to object.
     *
     * @param json JSON string
     * @return converted object
     * @throws JsonProcessingException syntax error
     */
    public static LLMResponse createLLMResponse(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, LLMResponse.class);
    }

    /**
     * Convert object to JSON string.
     *
     * @param object object to convert
     * @return JSON string
     * @throws JsonProcessingException syntax error
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}
