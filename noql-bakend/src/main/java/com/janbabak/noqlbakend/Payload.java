package com.janbabak.noqlbakend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Payload {
    protected static ObjectMapper objectMapper = new ObjectMapper();

    public String toJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }
}
