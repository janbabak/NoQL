package com.janbabak.noqlcustommodel.gptApi;

import com.janbabak.noqlcustommodel.publicApi.LlmMessage;
import com.janbabak.noqlcustommodel.publicApi.ModelRequest;

import java.util.List;

/**
 * GPT request object which is sent to the GPT API.
 */
public class GptRequest {
    public final String model = "gpt-3.5-turbo";
    public final List<LlmMessage> messages;

    /**
     * Create query
     *
     * @param chatHistory  chat history
     * @param queryRequest new natural language query, model, ...
     * @param systemQuery  instructions from the NoQL system about task that needs to be done
     * @param errors       list of errors from previous executions that should help the model fix its query
     */
    public GptRequest(ModelRequest request) {
        this.messages = request.messages();
    }
}
