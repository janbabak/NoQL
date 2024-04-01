package com.janbabak.noqlbackend.model.query;

import com.janbabak.noqlbackend.model.database.QueryResponse;

import java.util.List;

public class ChatResponse {

    private List<String> messages;
    private QueryResponse.QueryResult result;
    private Long totalCount;
    private String errorMessage;

}
