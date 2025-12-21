package com.janbabak.noqlbackend.service.langChain;

import com.janbabak.noqlbackend.error.exception.PlotScriptExecutionException;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.query.RetrievedData;
import com.janbabak.noqlbackend.service.PlotService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class AssistantTools {

    private final ExperimentalQueryService queryService;
    private final PlotService plotService;
    private final Database database;
    private final int page;
    private final int pageSize;
    private final String plotFileName;

    @Getter
    private ToolResult toolResult;

    public AssistantTools(Database database,
                          String plotFileName,
                          int page,
                          int pageSize,
                          ExperimentalQueryService queryService,
                          PlotService plotService) {
        this.database = database;
        this.plotFileName = plotFileName;
        this.page = page;
        this.pageSize = pageSize;
        this.queryService = queryService;
        this.plotService = plotService;
        this.toolResult = new ToolResult();
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
        toolResult.setDbQuery(query);
        try {
            RetrievedData retrievedData = queryService.executeQuery(query, database, page, pageSize);
            toolResult
                    .setRetrievedData(retrievedData)
                    .setDbQueryExecutedSuccessSuccessfully(true)
                    .setDbQueryExecutionErrorMessage(null);
        } catch (Exception e) {
            toolResult.setDbQueryExecutedSuccessSuccessfully(false);
            return handleError("Error while executing query", e, toolResult::setDbQueryExecutionErrorMessage);
        }
        log.info("Query executed successfully: {}", query);
        return ToolExecutionResult.success("Query executed successfully");
    }

    /**
     * Method is called by the LLM to generate a plot from data using provided Python code.
     * If the plot was generated successfully, the {@link #toolResult} field is updated accordingly.
     *
     * @param pythonCode Python code to generate the plot
     * @return tool execution result - info for the LLM about success or failure of the tool execution.
     */
    @Tool("Generate plot from data")
    public ToolExecutionResult generatePlot(@P("Pyton script") String pythonCode) {
        toolResult.setScript(pythonCode);
        try {
            plotService.generatePlot(pythonCode, database, plotFileName);
            toolResult
                    .setPlotGeneratedSuccessfully(true)
                    .setDbQueryExecutionErrorMessage(null);
        } catch (PlotScriptExecutionException e) {
            toolResult.setPlotGeneratedSuccessfully(false);
            return handleError("Error while executing plot", e, toolResult::setPlotGenerationErrorMessage);
        }
        log.info("Plot successfully generated, script: {}", plotFileName);
        return new ToolExecutionResult(true, null, "Plot successfully generated");
    }

    private ToolExecutionResult handleError(String context, Exception exception, Consumer<String> errorSetter) {
        String errorMessage = context + ": " + exception.getMessage();
        log.error(errorMessage);
        errorSetter.accept(errorMessage);
        return ToolExecutionResult.failure(errorMessage);
    }

    /**
     * Stores real results of tool execution.
     */
    @Data
    @Accessors(chain = true)
    public static class ToolResult {
        private Boolean dbQueryExecutedSuccessSuccessfully = null;
        private String dbQuery = null;
        private Boolean plotGeneratedSuccessfully = false;
        private String dbQueryExecutionErrorMessage = null;
        private String script = null;
        private RetrievedData retrievedData = null;
        private String plotGenerationErrorMessage = null;
    }

    /**
     * Result of tool execution - info for the LLM about success or failure of the tool execution.
     */
    public record ToolExecutionResult(
            boolean success,
            String message,
            String error) {

        public static ToolExecutionResult success(String message) {
            return new ToolExecutionResult(true, message, null);
        }

        public static ToolExecutionResult failure(String error) {
            return new ToolExecutionResult(false, null, error);
        }
    }
}
