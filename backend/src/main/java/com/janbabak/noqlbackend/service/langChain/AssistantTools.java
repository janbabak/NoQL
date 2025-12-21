package com.janbabak.noqlbackend.service.langChain;

import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.query.RetrievedData;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssistantTools {

    private final ExperimentalQueryService queryService;

    @SuppressWarnings("FieldMayBeFinal")
    private Database database;
    private final int page;
    private final int pageSize;

    @Getter
    private ToolResult toolResult;

    public AssistantTools(Database database,
                          int page,
                          int pageSize,
                          ExperimentalQueryService queryService) {
        this.database = database;
        this.page = page;
        this.pageSize = pageSize;
        this.queryService = queryService;
        this.toolResult = new ToolResult();
    }

    /**
     * Stores real results of tool execution.
     */
    @Data
    @Accessors(chain = true)
    public static class ToolResult {
        private RetrievedData retrievedData = null;
        private String plotUrl = null;
        private String error = null;
    }

    /**
     * Result of tool execution - info for the LLM about success or failure of the tool execution.
     */
    public record ToolExecutionResult(
            boolean success,
            String data,
            String error) {

        public static ToolExecutionResult success(String data) {
            return new ToolExecutionResult(true, data, null);
        }

        public static ToolExecutionResult failure(String error) {
            return new ToolExecutionResult(false, null, error);
        }
    }

    /**
     * Method is called by the LLM to execute a query on the database.
     * Real result is stored in {@link #toolResult} field.
     *
     * @param query database query in valid database query language
     * @return tool execution result - info for the LLM about success or failure of the tool execution.
     */
    @Tool("Execute query on database")
    public ToolExecutionResult executeQuery(@P("Database query in valid database query language") String query) {
        try {
            RetrievedData retrievedData = queryService.executeQuery(query, database, page, pageSize);
            toolResult
                    .setRetrievedData(retrievedData)
                    .setError(null);
        } catch (Exception e) {
            String error = "Error while executing query: " + e.getMessage();
            log.error(error);
            toolResult.setError(error);
            return ToolExecutionResult.failure(error);
        }
        log.info("Query executed successfully: {}", query);
        return ToolExecutionResult.success("Query executed successfully");
    }

    @Tool("Generate plot from data")
    public ToolExecutionResult generatePlot(@P("Pyton script") String pythonCode) {
        log.error("Not implemented yet: generatePlot: {}", pythonCode);
        return new ToolExecutionResult(true, null, "Plot successfully generated");
    }
}
