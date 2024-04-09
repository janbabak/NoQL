package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.gpt.GptQuery;
import com.janbabak.noqlbackend.model.query.gpt.GptResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@NoArgsConstructor
public class GptApi implements QueryApi {

    @SuppressWarnings("all")
    private final String GPT_URL = "https://api.openai.com/v1/chat/completions";
    private final String token = System.getenv("API_KEY");
    private final RestTemplate restTemplate = new RestTemplate();
    public String gptModel = GptQuery.GPT_3_5_TURBO;

    /**
     * Send queries in chat form the model and retrieve a response.
     *
     * @param chatHistory chat history
     * @param query       users query
     * @param systemQuery instructions from the NoQL system about task that needs to be done
     * @param errors      list of errors from previous executions that should help the model fix its query
     * @return model's response
     * @throws LLMException when LLM request fails.
     */
    @Override
    public String queryModel(
            List<ChatQueryWithResponse> chatHistory,
            String query,
            String systemQuery,
            List<String> errors) throws LLMException {

        log.info("Chat with GPT API.");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<GptQuery> request = new HttpEntity<>(
                new GptQuery(chatHistory, query, systemQuery, errors, gptModel), headers);

        ResponseEntity<GptResponse> responseEntity = restTemplate.exchange(
                GPT_URL, HttpMethod.POST, request, GptResponse.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return !Objects.requireNonNull(responseEntity.getBody()).getChoices().isEmpty()
                    ? responseEntity.getBody().getChoices().get(0).getMessage().getContent()
                    : null;
        }
        if (responseEntity.getStatusCode().is4xxClientError()) {
            log.error("Bad request to the GPT model, status_code={}, response={}.",
                    responseEntity.getStatusCode(), responseEntity.getBody());
            throw new LLMException("Bad request to the GPT model, we are working on it.");
        }
        if (responseEntity.getStatusCode().is5xxServerError()) {
            log.error("Error on GPT side, status_code={}, response={}.",
                    responseEntity.getStatusCode(), responseEntity.getBody());
            throw new LLMException("Error on GPT side, try it latter");
        }
        return null;
    }
}
