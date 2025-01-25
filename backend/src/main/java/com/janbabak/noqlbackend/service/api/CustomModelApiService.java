package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.CustomModel;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.model.query.customModel.CustomModelRequest;
import com.janbabak.noqlbackend.model.query.customModel.CustomModelResponse;
import com.janbabak.noqlbackend.service.CustomModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service responsible for sending queries to custom models.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomModelApiService implements QueryApi {

    private final CustomModelService customModelService;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send queries in chat form the model and retrieve a response.
     *
     * @param chatHistory  chat history
     * @param queryRequest users query and model
     * @param systemQuery  instructions from the NoQL system about task that needs to be done
     * @param errors       list of errors from previous executions that should help the model fix its query
     * @return model's response
     * @throws LLMException            when LLM request fails.
     * @throws BadRequestException     when queryRequest is not valid
     * @throws EntityNotFoundException when model is not found
     */
    @Override
    public String queryModel(
            List<ChatQueryWithResponse> chatHistory,
            QueryRequest queryRequest,
            String systemQuery,
            List<String> errors) throws LLMException, BadRequestException, EntityNotFoundException {

        log.info("Chat with custom model API.");

        UUID modelId = validateRequest(queryRequest);

        CustomModel model = customModelService.findById(modelId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<CustomModelRequest> request = new HttpEntity<>(
                new CustomModelRequest(chatHistory, queryRequest, systemQuery, errors), headers);

        ResponseEntity<CustomModelResponse> responseEntity;

        try {
            responseEntity = restTemplate.postForEntity(model.getUrl(), request, CustomModelResponse.class);
        } catch (RestClientException e) {
            log.error("Error while calling Custom model API. {}", e.getMessage());
            throw new LLMException("Error while calling Custom model API, try it latter.");
        }

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return !Objects.requireNonNull(responseEntity.getBody()).choices().isEmpty()
                    ? responseEntity.getBody().choices().get(0).message().getContent()
                    : null;
        }
        if (responseEntity.getStatusCode().is4xxClientError()) {
            log.error("Bad request to the Custom model, status_code={}, response={}.",
                    responseEntity.getStatusCode(), responseEntity.getBody());
            throw new LLMException("Bad request to the Custom model, we are working on it.");
        }
        if (responseEntity.getStatusCode().is5xxServerError()) {
            log.error("Error on Custom side, status_code={}, response={}.",
                    responseEntity.getStatusCode(), responseEntity.getBody());
            throw new LLMException("Error on Custom side, try it latter");
        }
        return null;
    }

    private UUID validateRequest(QueryRequest queryRequest) throws BadRequestException {
        try {
            return UUID.fromString(queryRequest.getModel());
        } catch (Exception e) {
            throw new BadRequestException("Model is not in UUID format.");
        }
    }
}
