package com.janbabak.noqlbackend.service.api;

import org.json.JSONObject;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

public class AwsService {

//    BedrockRuntimeClient bedrockClient;
//
//    public AwsService() {
//        bedrockClient = BedrockRuntimeClient.builder()
//                .region(Region.EU_CENTRAL_1)
//                .credentialsProvider(ProfileCredentialsProvider.create())
//                .build();
//    }

    public static void main(String[] args) {
        BedrockRuntimeClient bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();


        String prompt = "what is the most common name";

        JSONObject jsonBody = new JSONObject()
                .put("prompt", "Human: " + prompt + " Assistant:")
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
        System.out.println(responseAsJson.toString());
    }


}
