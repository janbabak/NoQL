package com.janbabak.noqlcustommodel.gptApi;

import com.janbabak.noqlcustommodel.publicApi.ModelRequest;
import com.janbabak.noqlcustommodel.publicApi.ModelResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@NoArgsConstructor
public class GptApiService {

    @SuppressWarnings("all")
    private final String GPT_URL = "https://api.openai.com/v1/chat/completions";
    private final String token = System.getenv("GPT_API_KEY");
    private final RestTemplate restTemplate = new RestTemplate();

    public ModelResponse queryModel(ModelRequest request) {

        log.info("Chat with GPT API.");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<GptRequest> gptRequest = new HttpEntity<>(new GptRequest(request), headers);

        ResponseEntity<GptResponse> responseEntity = restTemplate.exchange(
                GPT_URL, HttpMethod.POST, gptRequest, GptResponse.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return !Objects.requireNonNull(responseEntity.getBody()).getChoices().isEmpty()
                    ? new ModelResponse(responseEntity.getBody())
                    : null;
        } else if (responseEntity.getStatusCode().is4xxClientError()) {
            log.error("Bad request to the GPT model, status_code={}, response={}.",
                    responseEntity.getStatusCode(), responseEntity.getBody());
            throw new InternalError("Bad request to the GPT model, we are working on it.");
        } else if (responseEntity.getStatusCode().is5xxServerError()) {
            log.error("Error on GPT side, status_code={}, response={}.",
                    responseEntity.getStatusCode(), responseEntity.getBody());
            throw new InternalError("Error on GPT side, try it latter");
        }
        return null;
    }
}
