package com.janbabak.noqlbakend.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.janbabak.noqlbakend.Payload;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class GptQuery extends Payload {
    public String model;
    public List<Message> messages;

    public GptQuery(String query, String model) {
        this.model = model;
        this.messages = List.of(new Message("user", query));
    }

    @AllArgsConstructor
    public static class Message {
        public String role;
        public String content;
    }

    public static void main(String[] args) throws JsonProcessingException {
        GptQuery query = new GptQuery("gpt-3.5-turbo", List.of(new Message("user", "How are you?")));

        System.out.println(query.toJson());
    }
}
