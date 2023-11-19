package com.janbabak.noqlbakend.service.api;

import com.janbabak.noqlbakend.model.GptQuery;
import org.springframework.http.MediaType;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

public class GptApi implements QueryApi {

    private final URL url;

    private final String token = System.getenv("API_KEY");

    public String gptModel = "gpt-3.5-turbo";

    public GptApi() throws URISyntaxException, MalformedURLException {
        this.url = new URI("https://api.openai.com/v1/chat/completions").toURL();
    }

    public String queryModel(String query) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // set connection properties
        connection.setRequestMethod("POST");
        connection.setRequestProperty(AUTHORIZATION, "Bearer " + this.token);
        connection.setRequestProperty(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        connection.setDoOutput(true);

        // input data
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);

        outputStreamWriter.write(new GptQuery(query, gptModel).toJson());
        outputStreamWriter.flush();
        outputStreamWriter.close();
        connection.getOutputStream().close();
        connection.connect();

        if (connection.getResponseCode() >= 300) {
            throw new Exception("Retrieving query failed.");
        }

        // output data
        InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder responseContent = new StringBuilder();
        String inputLine;
        while ((inputLine = bufferedReader.readLine()) != null) {
            responseContent.append(inputLine);
        }
        bufferedReader.close();
        inputStreamReader.close();
        connection.disconnect();

        return responseContent.toString();
    }
}
