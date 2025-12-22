package com.janbabak.noqlbackend.service.langChain;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.PlotService;
import com.janbabak.noqlbackend.service.query.QueryExecutionService;
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

    private final QueryExecutionService queryService;
    private final PlotService plotService;

    public LLMServiceResult executeUserRequest(LLMServiceRequest request){

        ChatModel model = getModel(request.modelId);
        int page = 0;
        QueryDatabaseAssistantTools assistantTools = new QueryDatabaseAssistantTools(
                request.database,
                request.plotFileName,
                page,
                request.pageSize,
                queryService,
                plotService);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(assistantTools)
                .build();
        List<ChatMessage> messages = buildMessages(request);

        String response = assistant.chat(messages);
        log.info("LLM response: {}", response);

        QueryDatabaseToolResult toolResult = assistantTools.getToolResult();
        log.info("LLM tool result: {}", toolResult);

        return new LLMServiceResult(response, toolResult);
    }

    public record LLMServiceRequest(
            String userQuery,
            String systemQuery,
            Database database,
            String plotFileName,
            String modelId,
            int pageSize,
            List<ChatQueryWithResponse> chatHistory
    ) {
    }

    private List<ChatMessage> buildMessages(LLMServiceRequest request) {
        List<ChatMessage> messages = new ArrayList<>();

        if (request.systemQuery != null && !request.systemQuery.isBlank()) {
            messages.add(SystemMessage.from(request.systemQuery));
        }

        for (ChatQueryWithResponse chatEntry : request.chatHistory) {
            messages.add(UserMessage.from(chatEntry.getNlQuery()));
            messages.add(AiMessage.from(buildLLMResponseMessage(chatEntry)));
        }

        messages.add(UserMessage.from(request.userQuery));

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
