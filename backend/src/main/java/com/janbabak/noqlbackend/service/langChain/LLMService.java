package com.janbabak.noqlbackend.service.langChain;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import dev.langchain4j.data.message.AiMessage;
import com.janbabak.noqlbackend.service.langChain.AssistantTools.ToolResult;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofSeconds;

@Slf4j
@Service
@RequiredArgsConstructor
public class LLMService {

    @SuppressWarnings("all") // value is never assigned
    @Value("${app.externalServices.openAiApi.apiKey}")
    private String openAiApiKey;

    private final ExperimentalQueryService queryService;

    public LLMServiceResult executeUserRequest(
            String userQuery,
            String systemQuery,
            Database database,
            String modelId,
            int pageSize,
            List<ChatQueryWithResponse> chatHistory) {

        ChatModel model = getModel(modelId);
        AssistantTools assistantTools = new AssistantTools(database, 0, pageSize, queryService);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(assistantTools)
                .build();

        List<ChatMessage> messages = buildMessages(userQuery, systemQuery, chatHistory);

        String response = assistant.chat(messages);
        log.info("LLM response: {}", response);

        ToolResult toolResult = assistantTools.getToolResult();
        log.info("LLM tool result: {}", toolResult);

        return new LLMServiceResult(response, toolResult);
    }

    private ChatModel getModel(String modelId) {
        // TODO: implement model selection based on modelId

        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.0)
                .timeout(ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .httpClientBuilder(new SpringRestClientBuilderFactory().create())
                .build();
    }

    private List<ChatMessage> buildMessages(String userQuery, String systemQuery, List<ChatQueryWithResponse> chatHistory) {
        List<ChatMessage> messages = new ArrayList<>();

        if (systemQuery != null && !systemQuery.isBlank()) {
            messages.add(SystemMessage.from(systemQuery));
        }

        for (ChatQueryWithResponse chatEntry : chatHistory) {
            messages.add(UserMessage.from(chatEntry.getNlQuery()));
            messages.add(AiMessage.from(chatEntry.getLlmResponse()));
        }

        messages.add(UserMessage.from(userQuery));

        return messages;
    }

    public record LLMServiceResult(
            String llmResponse, // response from LLM, comment about the execution
            ToolResult toolResult // real result of the tool execution
    ) {
    }
}
