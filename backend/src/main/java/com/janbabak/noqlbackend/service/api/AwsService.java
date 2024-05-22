package com.janbabak.noqlbackend.service.api;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import org.json.JSONObject;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.List;

@Deprecated // unused
@SuppressWarnings("all")
public class AwsService implements QueryApi {

    private final BedrockRuntimeClient bedrockClient = BedrockRuntimeClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(ProfileCredentialsProvider.create())
            .build();

    public static void main(String[] args) {
        BedrockRuntimeClient bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();


        String prompt = "what is the most common name";

        JSONObject jsonBody = new JSONObject()
                .put("prompt",
                        """
                        System: You are an assistant that translates user's queries into an SQL language. Your response must contain only the SQL query and nothing more.
                        
                        Human: find all users that are older than 24.
                        
                        Assistant:
                        """)
                .put("temperature", 0.5)
                .put("top_p", 0.9)
                .put("max_gen_len", 1024);

        InvokeModelRequest invokeModelRequest = InvokeModelRequest.builder()
                .modelId("meta.llama2-70b-chat-v1")
                .body(SdkBytes.fromUtf8String(jsonBody.toString()))
                .build();


        InvokeModelResponse response = bedrockClient.invokeModel(invokeModelRequest);
        JSONObject responseAsJson = new JSONObject(response.body().asUtf8String());
        System.out.println("response: ");
        System.out.println(responseAsJson);
    }

    @Override
    public String queryModel(List<ChatQueryWithResponse> chatHistory, QueryRequest queryRequest, String systemQuery, List<String> errors) {
        JSONObject jsonBody = new JSONObject()
                .put("prompt", buildPrompt(chatHistory, queryRequest.getQuery(), systemQuery))
                .put("temperature", 0.5)
                .put("top_p", 0.9)
                .put("max_gen_len", 512);

        InvokeModelRequest invokeModelRequest = InvokeModelRequest.builder()
                .modelId("meta.llama2-70b-chat-v1")
                .body(SdkBytes.fromUtf8String(jsonBody.toString()))
                .build();

        InvokeModelResponse response = bedrockClient.invokeModel(invokeModelRequest);
        System.out.println(response.body().asUtf8String());

        return response.body().asUtf8String();
    }

    private String buildPrompt(List<ChatQueryWithResponse> chatHistory, String query, String systemQuery) {
        final String systemRole = "\n\nSystem: ";
        final String userRole = "\n\nUser: ";
        final String assistantRole = "\n\nAssistant: ";

        StringBuilder stringBuilder = new StringBuilder(systemRole + systemQuery);

        for (ChatQueryWithResponse chatQueryWithResponse: chatHistory) {
            stringBuilder
                    .append(userRole)
                    .append(chatQueryWithResponse.getNlQuery())
                    .append(assistantRole)
                    .append(chatQueryWithResponse.getLlmResponse());
        }
        stringBuilder
                .append(userRole)
                .append(query)
                .append(assistantRole);

        return stringBuilder.toString();
    }
}
