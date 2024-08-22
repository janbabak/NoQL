package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.model.query.llama.LlamaRequest;
import com.janbabak.noqlbackend.model.query.llama.LlamaResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@NoArgsConstructor
public class LlamaApiService implements QueryApi {

    @SuppressWarnings("all")
    private final String LLAMA_API_URL = "https://api.llama-api.com/chat/completions";
    private final String token = System.getenv("LLAMA_AUTH_TOKEN");
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send queries in chat form the model and retrieve a response.
     *
     * @param chatHistory  chat history
     * @param queryRequest users query, model...
     * @param systemQuery  instructions from the NoQL system about task that needs to be done
     * @param errors       list of errors from previous executions that should help the model fix its query
     * @return model's response
     * @throws LLMException when LLM request fails.
     * @throws BadRequestException when queryRequest is not valid
     */
    @Override
    public String queryModel(
            List<ChatQueryWithResponse> chatHistory,
            QueryRequest queryRequest,
            String systemQuery,
            List<String> errors) throws LLMException, BadRequestException {

        log.info("Chat with Llama API");

        validateRequest(queryRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<LlamaRequest> request = new HttpEntity<>(
                new LlamaRequest(chatHistory, queryRequest, systemQuery, errors), headers);

        ResponseEntity<LlamaResponse> responseEntity;

        try {
            responseEntity = restTemplate.exchange(LLAMA_API_URL, HttpMethod.POST, request, LlamaResponse.class);
        } catch (RestClientException e) {
            log.error("Error while calling Llama API. {}", e.getMessage());
            throw new LLMException("Error while calling Llama API, try it latter.");
        }

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return !Objects.requireNonNull(responseEntity.getBody()).getChoices().isEmpty()
                    ? responseEntity.getBody().getChoices().get(0).getMessage().getContent()
                    : null;
        }
        if (responseEntity.getStatusCode().is4xxClientError()) {
            log.error("Bad request to the Llama model, status_code={}, response={}.",
                    responseEntity.getStatusCode(), responseEntity.getBody());
            throw new LLMException("Bad request to the Llama model, we are working on it.");
        }
        if (responseEntity.getStatusCode().is5xxServerError()) {
            log.error("Error on Llama side, status_code={}, response={}.",
                    responseEntity.getStatusCode(), responseEntity.getBody());
            throw new LLMException("Error on Llama side, try it latter");
        }
        return null;
    }

    /**
     * Validate request
     *
     * @param queryRequest users request
     * @throws BadRequestException unsupported model
     */
    void validateRequest(QueryRequest queryRequest) throws BadRequestException {
        if (queryRequest.getModel() == null || !queryRequest.getModel().startsWith("llama")) {
            log.error("Unsupported model: {}", queryRequest.getModel());
            throw new BadRequestException("Only Llama models are supported.");
        }
    }
}
