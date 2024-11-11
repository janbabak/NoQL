package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.model.query.claude.ClaudeRequest;
import com.janbabak.noqlbackend.model.query.claude.ClaudeResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@NoArgsConstructor
public class ClaudeApiService implements QueryApi {

    @Value("${app.externalServices.claudeApi.url}")
    private String claudeUrl;

    @Value("${app.externalServices.claudeApi.apiKey}")
    private String apiKey;

    @Value("${app.externalServices.claudeApi.anthropicVersion}")
    private String anthropicVersion;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send queries in chat form the model and retrieve a response.
     *
     * @param chatHistory  chat history
     * @param queryRequest users query and model
     * @param systemQuery  instructions from the NoQL system about task that needs to be done
     * @param errors       list of errors from previous executions that should help the model fix its query
     * @return model's response
     * @throws LLMException        when LLM request fails.
     * @throws BadRequestException when queryRequest is not valid
     */
    @Override
    public String queryModel(
            List<ChatQueryWithResponse> chatHistory,
            QueryRequest queryRequest,
            String systemQuery,
            List<String> errors) throws LLMException, BadRequestException {

        log.info("Chat with Claude API.");

        validateRequest(queryRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("x-api-key", apiKey);
        headers.add("anthropic-version", anthropicVersion);

        HttpEntity<ClaudeRequest> request = new HttpEntity<>(
                new ClaudeRequest(chatHistory, queryRequest, systemQuery, errors), headers);

        ResponseEntity<ClaudeResponse> responseEntity;

        try {
            responseEntity = restTemplate.postForEntity(claudeUrl, request, ClaudeResponse.class);
        } catch (RestClientException e) {
            log.error("Claude API request failed: {}", e.getMessage());
            throw new LLMException("Error while calling Claude API, try it later.");
        }

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return !Objects.requireNonNull(responseEntity.getBody()).content().isEmpty()
                    ? responseEntity.getBody().content().get(0).text()
                    : null;
        }
        if (responseEntity.getStatusCode().is4xxClientError()) {
            log.error("Bad request to the Claude model, status_code={}, response={}.",
                    responseEntity.getStatusCode(), responseEntity.getBody());
            throw new LLMException("Bad request to the Claude model, we are working on it.");
        }
        if (responseEntity.getStatusCode().is5xxServerError()) {
            log.error("Error on Claude side, status_code={}, response={}.",
                    responseEntity.getStatusCode(), responseEntity.getBody());
            throw new LLMException("Error on Gemini side, try it latter");
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
        if (queryRequest.getModel() == null || !queryRequest.getModel().startsWith("claude")) {
            log.error("Model is missing in the request.");
            throw new BadRequestException("Model is missing in the request.");
        }
    }
}
