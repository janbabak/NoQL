package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.llama.LlamaQuery;
import com.janbabak.noqlbackend.model.query.llama.LlamaResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Slf4j
@NoArgsConstructor
public class LlamaApiService implements QueryApi {

    @SuppressWarnings("all")
    private final String LLAMA_API_URL = "https://api.llama-api.com/chat/completions";
    private final String token = System.getenv("LLAMA_AUTH_TOKEN");
    private final RestTemplate restTemplate = new RestTemplate();
    public String llamaModel = LlamaQuery.LLAMA3_13B_CHAT;

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

        log.info("Chat with Llama API");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        LlamaQuery llamaQuery = new LlamaQuery(chatHistory, query, systemQuery, errors, llamaModel);
        System.out.println(llamaQuery);
        HttpEntity<LlamaQuery> request = new HttpEntity<>(
                llamaQuery, headers);

        ResponseEntity<LlamaResponse> responseEntity = restTemplate.exchange(
                LLAMA_API_URL, HttpMethod.POST, request, LlamaResponse.class);

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
}
