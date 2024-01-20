package com.janbabak.noqlbackend.service.api;

/**
 * API which handles queries to  the LLMs
 */
public interface QueryApi {

    /**
     * Send query to the model and retrieve a response.
     * @param query that is sent to the model
     * @return model's response
     * @throws Exception when API request fails.
     */
    String queryModel(String query) throws Exception;
}
