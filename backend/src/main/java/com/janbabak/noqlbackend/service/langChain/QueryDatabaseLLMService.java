package com.janbabak.noqlbackend.service.langChain;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.PlotService;
import com.janbabak.noqlbackend.service.query.QueryExecutionService;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import com.janbabak.noqlbackend.service.langChain.QueryDatabaseAssistantTools.QueryDatabaseToolResult;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.AiServices;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryDatabaseLLMService extends BaseLLMService {

    private final QueryExecutionService queryService;
    private final PlotService plotService;

    public LLMServiceResult executeUserRequest(LLMServiceRequest request) throws BadRequestException {

        int page = 0;
        QueryDatabaseAssistantTools assistantTools = new QueryDatabaseAssistantTools(
                request.database,
                request.plotFileName,
                page,
                request.pageSize,
                queryService,
                plotService);

        Assistant assistant = buildAssistant(request.modelId, assistantTools);
        List<ChatMessage> messages = buildMessages(request);

        String response = assistant.chat(messages);
        log.info("LLM response: {}", response);

        QueryDatabaseToolResult toolResult = assistantTools.getToolResult();
        log.info("LLM tool result: {}", toolResult);

        return new LLMServiceResult(response, toolResult);
    }

    Assistant buildAssistant(String modelId, QueryDatabaseAssistantTools assistantTools)
            throws BadRequestException {

        return AiServices.builder(Assistant.class)
                .chatModel(getModel(modelId))
                .tools(assistantTools)
                .build();
    }

    @Builder
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

    /**
     * Build chat messages including system message, chat history and user query.
     */
    private List<ChatMessage> buildMessages(LLMServiceRequest request) {
        List<ChatMessage> messages = new ArrayList<>();

        if (request.systemQuery != null && !request.systemQuery.isBlank()) {
            messages.add(SystemMessage.from(request.systemQuery));
        }

        for (ChatQueryWithResponse chatEntry : request.chatHistory) {
            if (chatEntry.getNlQuery() == null || chatEntry.getNlQuery().isBlank()) {
                continue;
            }
            messages.add(UserMessage.from(chatEntry.getNlQuery()));
            messages.add(buildAiMessage(chatEntry));
        }

        messages.add(UserMessage.from(request.userQuery));

        return messages;
    }

    private AiMessage buildAiMessage(ChatQueryWithResponse chatEntry) {
        List<ToolExecutionRequest> toolExecutionRequests = new ArrayList<>();

        Map<String, Object> attributes = new HashMap<>();

        if (chatEntry.dbQueryExecuted()) {
            toolExecutionRequests.add(
                    ToolExecutionRequest.builder()
                            .name("executeQuery")
                            .arguments(chatEntry.getDbQuery())
                            .build());
            attributes.put("executeQuerySuccess", chatEntry.getDbQueryExecutionSuccess());

            if (chatEntry.getDbExecutionErrorMessage() != null) {
                attributes.put("executeQueryError", chatEntry.getDbExecutionErrorMessage());
            }
        }
        if (chatEntry.plotGenerated()) {
            toolExecutionRequests.add(
                    ToolExecutionRequest.builder()
                            .name("generatePlot")
                            .arguments(chatEntry.getPlotScript())
                            .build());
            attributes.put("generatePlotSuccess", chatEntry.getPlotGenerationSuccess());

            if (chatEntry.getPlotGenerationErrorMessage() != null) {
                attributes.put("generatePlotError", chatEntry.getPlotGenerationErrorMessage());
            }
        }

        return AiMessage.builder()
                .text(chatEntry.getResultDescription())
                .toolExecutionRequests(toolExecutionRequests)
                .attributes(attributes)
                .build();
    }

    public record LLMServiceResult(
            String llmResponse, // response from LLM, comment about the execution
            QueryDatabaseToolResult toolResult // real result of the tool execution
    ) {
    }
}
