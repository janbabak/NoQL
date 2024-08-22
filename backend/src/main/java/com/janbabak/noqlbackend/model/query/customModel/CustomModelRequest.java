package com.janbabak.noqlbackend.model.query.customModel;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.model.query.gpt.GptRequest;

import java.util.List;

/**
 * Custom model request object which is sent to the custom model API. It is the same as GptRequest.
 */
public class CustomModelRequest extends GptRequest {
    /**
     * Create query
     *
     * @param chatHistory  chat history
     * @param queryRequest new natural language query, model, ...
     * @param systemQuery  instructions from the NoQL system about task that needs to be done
     * @param errors       list of errors from previous executions that should help the model fix its query
     */
    public CustomModelRequest(List<ChatQueryWithResponse> chatHistory,
                              QueryRequest queryRequest,
                              String systemQuery,
                              List<String> errors) {

        super(chatHistory, queryRequest, systemQuery, errors);
    }
}
