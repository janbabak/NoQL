package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.query.ChatRequest;

/**
 * API which handles queries to  the LLMs
 */
public interface QueryApi {

    /**
     * Send query to the model and retrieve a response.
     *
     * @param query that is sent to the model
     * @return model's response
     * @throws LLMException when LLM request fails.
     */
    String queryModel(String query) throws LLMException;

    /**
     * Send queries in chat form the model and retrieve a response.
     *
     * @param chat that is sent to the model
     * @return model's response
     * @throws LLMException when LLM request fails.
     */
    String queryModel(ChatRequest chat) throws LLMException;
}
