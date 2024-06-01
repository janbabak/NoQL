package com.janbabak.noqlbackend.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create object from JSON string.
     * @param json JSON string
     * @param clazz class of object
     * @throws JsonProcessingException JSON syntax error
     */
    public static <T> T createFromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Convert object to JSON string.
     *
     * @param object object to convert
     * @return JSON string
     * @throws JsonProcessingException JSON syntax error
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}
