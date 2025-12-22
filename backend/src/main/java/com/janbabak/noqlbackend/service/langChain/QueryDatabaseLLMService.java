package com.janbabak.noqlbackend.service.langChain;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.PlotService;
import dev.langchain4j.data.message.AiMessage;
import com.janbabak.noqlbackend.service.langChain.QueryDatabaseAssistantTools.QueryDatabaseToolResult;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryDatabaseLLMService extends BaseLLMService {

    private final ExperimentalQueryService queryService;
    private final PlotService plotService;

    public LLMServiceResult executeUserRequest(
            String userQuery,
            String systemQuery,
            Database database,
            String plotFileName,
            String modelId,
            int pageSize,
            List<ChatQueryWithResponse> chatHistory) {

        ChatModel model = getModel(modelId);
        QueryDatabaseAssistantTools assistantTools = new QueryDatabaseAssistantTools(
                database, plotFileName, 0, pageSize, queryService, plotService);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(assistantTools)
                .build();
        List<ChatMessage> messages = buildMessages(userQuery, systemQuery, chatHistory);

        String response = assistant.chat(messages);
        log.info("LLM response: {}", response);

        QueryDatabaseToolResult toolResult = assistantTools.getToolResult();
        log.info("LLM tool result: {}", toolResult);

        return new LLMServiceResult(response, toolResult);
    }

    private List<ChatMessage> buildMessages(String userQuery, String systemQuery, List<ChatQueryWithResponse> chatHistory) {
        List<ChatMessage> messages = new ArrayList<>();

        if (systemQuery != null && !systemQuery.isBlank()) {
            messages.add(SystemMessage.from(systemQuery));
        }

        for (ChatQueryWithResponse chatEntry : chatHistory) {
            messages.add(UserMessage.from(chatEntry.getNlQuery()));
            messages.add(AiMessage.from(buildLLMResponseMessage(chatEntry)));
        }

        messages.add(UserMessage.from(userQuery));

        return messages;
    }

    /**
     * Build message that is sent to LLM as AI response in the chat history.
     */
    private String buildLLMResponseMessage(ChatQueryWithResponse chatEntry) {
        StringBuilder stringBuilder = new StringBuilder("LLM Response: ")
                .append(chatEntry.getResultDescription())
                .append("\n\n Executed tools:\n");

        if (chatEntry.getDbQuery() != null) {
            stringBuilder.append("\nMethod call: executeQuery(\"");
            stringBuilder.append(chatEntry.getDbQuery());
            stringBuilder.append("\")\n");
            stringBuilder.append("Success: ");
            stringBuilder.append(chatEntry.getDbQueryExecutionSuccess());

            if (chatEntry.getDbExecutionErrorMessage() != null) {
                stringBuilder.append("\nErrors: ");
                stringBuilder.append(chatEntry.getDbExecutionErrorMessage());
                stringBuilder.append("\n");
            }
        }

        if (chatEntry.getDbQuery() != null) {
            stringBuilder.append("\nMethod call: generatePlot(\"");
            stringBuilder.append(chatEntry.getPlotScript());
            stringBuilder.append("\")\n");
            stringBuilder.append("Success: ");
            stringBuilder.append(chatEntry.getPlotGenerationSuccess());

            if (chatEntry.getPlotGenerationErrorMessage() != null) {
                stringBuilder.append("\nErrors: ");
                stringBuilder.append(chatEntry.getPlotGenerationErrorMessage());
            }
        }

        return stringBuilder.toString();
    }

    public record LLMServiceResult(
            String llmResponse, // response from LLM, comment about the execution
            QueryDatabaseToolResult toolResult // real result of the tool execution
    ) {
    }
}
