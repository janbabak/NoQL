package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.query.ChatRequest;

import java.util.List;

/**
 * API which handles queries to  the LLMs
 */
public interface QueryApi {

    /**
     * Send queries in chat form the model and retrieve a response.
     *
     * @param chat        that is sent to the model
     * @param systemQuery instructions from the NoQL system about task that needs to be done
     * @param errors      list of errors from previous executions that should help the model fix its query
     * @return model's response
     * @throws LLMException when LLM request fails.
     */
    String queryModel(ChatRequest chat, String systemQuery, List<String> errors) throws LLMException;
}
