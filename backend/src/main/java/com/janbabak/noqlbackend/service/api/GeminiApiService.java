package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.model.query.gemini.GeminiRequest;
import com.janbabak.noqlbackend.model.query.gemini.GeminiResponse;
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
public class GeminiApiService implements QueryApi {

    @Value("${app.externalServices.geminiApi.url}")
    private String geminiUrl;

    @Value("${app.externalServices.geminiApi.apiKey}")
    private String token;

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

        log.info("Chat with Gemini API.");

        validateRequest(queryRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<GeminiRequest> request = new HttpEntity<>(
                new GeminiRequest(chatHistory, queryRequest, systemQuery, errors), headers);

        String url = geminiUrl + "/" + queryRequest.getModel() + ":generateContent?key=" + token;

        ResponseEntity<GeminiResponse> responseEntity;

        try {
            responseEntity = restTemplate.postForEntity(url, request, GeminiResponse.class);
        } catch (RestClientException e) {
            log.error("Gemini API request failed: {}", e.getMessage());
            throw new LLMException("Error while calling Gemini API, try it later.");
        }

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return !Objects.requireNonNull(responseEntity.getBody()).candidates().isEmpty()
                    ? responseEntity.getBody().candidates().get(0).content().parts().get(0).text()
                    : null;
        }
        if (responseEntity.getStatusCode().is4xxClientError()) {
            log.error("Bad request to the Gemini model, status_code={}, response={}.",
                    responseEntity.getStatusCode(), responseEntity.getBody());
            throw new LLMException("Bad request to the Gemini model, we are working on it.");
        }
        if (responseEntity.getStatusCode().is5xxServerError()) {
            log.error("Error on Gemini side, status_code={}, response={}.",
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
        if (queryRequest.getModel() == null || !queryRequest.getModel().startsWith("gemini")) {
            log.error("Model is missing in the request.");
            throw new BadRequestException("Model is missing in the request.");
        }
    }
}
