package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.model.gpt.GptQuery;
import com.janbabak.noqlbackend.model.gpt.GptResponse;
import lombok.NoArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Objects;

@NoArgsConstructor
public class GptApi implements QueryApi {

    @SuppressWarnings("all")
    private final String GPT_URL = "https://api.openai.com/v1/chat/completions";
    private final String token = System.getenv("API_KEY");
    private final RestTemplate restTemplate = new RestTemplate();
    public String gptModel = GptQuery.GPT_3_5_TURBO;

    @SuppressWarnings("unused")
    public GptApi(String gptModel) {
        this.gptModel = gptModel;
    }

    public String queryModel(String query) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<GptQuery> request = new HttpEntity<>(new GptQuery(query, gptModel), headers);

        ResponseEntity<GptResponse> responseEntity = restTemplate.exchange(
                GPT_URL, HttpMethod.POST, request, GptResponse.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return !Objects.requireNonNull(responseEntity.getBody()).getChoices().isEmpty()
                    ? responseEntity.getBody().getChoices().get(0).getMessage().getContent()
                    : null;
        }
        if (responseEntity.getStatusCode().is4xxClientError()) {
            throw new Exception("Bad request, we are working on it, try it latter.");
        }
        if (responseEntity.getStatusCode().is5xxServerError()) {
            throw new Exception("Error on GPT side, try it latter");
        }
        return null;
    }
}
