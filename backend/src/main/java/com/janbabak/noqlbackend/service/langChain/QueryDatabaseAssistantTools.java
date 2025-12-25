package com.janbabak.noqlbackend.service.langChain;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.PlotScriptExecutionException;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.query.RetrievedData;
import com.janbabak.noqlbackend.service.PlotService;
import com.janbabak.noqlbackend.service.query.QueryExecutionService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;

import java.sql.SQLException;
import java.util.function.Consumer;

@Slf4j
public class QueryDatabaseAssistantTools {

    private final QueryExecutionService queryService;
    private final PlotService plotService;
    private final Database database;
    private final int page;
    private final int pageSize;
    private final String plotFileName;

    @Getter
    private QueryDatabaseToolResult toolResult;

    public QueryDatabaseAssistantTools(Database database,
                                       String plotFileName,
                                       int page,
                                       int pageSize,
                                       QueryExecutionService queryService,
                                       PlotService plotService) {
        this.database = database;
        this.plotFileName = plotFileName;
        this.page = page;
        this.pageSize = pageSize;
        this.queryService = queryService;
        this.plotService = plotService;
        this.toolResult = new QueryDatabaseToolResult();
    }

    /**
     * Method is called by the LLM to execute a query on the database.
     * Real result is stored in {@link #toolResult} field.
     *
     * @param query database query in valid database query language
     * @return tool execution result - info for the LLM about success or failure of the tool execution.
     */
    @SuppressWarnings("unused")
    @Tool("Execute query on database")
    public ToolExecutionResult executeQuery(@P("Database query in valid database query language") String query)  {
        log.info("Execute query tool called");
        toolResult.setDbQuery(query);
        try {
            RetrievedData retrievedData = queryService.executeQuery(query, database, page, pageSize);
            toolResult
                    .setRetrievedData(retrievedData)
                    .setDbQueryExecutedSuccessSuccessfully(true)
                    .setDbQueryExecutionErrorMessage(null);
        } catch (DatabaseConnectionException | SQLException | DatabaseExecutionException | BadRequestException e) {
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
    @SuppressWarnings("unused")
    @Tool("Generate plot from data")
    public ToolExecutionResult generatePlot(@P("Pyton script") String pythonCode) {
        log.info("Generate plot tool called");
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
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class QueryDatabaseToolResult {
        @Builder.Default
        private Boolean dbQueryExecutedSuccessSuccessfully = null;

        @Builder.Default
        private String dbQuery = null;

        @Builder.Default
        private Boolean plotGeneratedSuccessfully = false;

        @Builder.Default
        private String dbQueryExecutionErrorMessage = null;

        @Builder.Default
        private String script = null;

        @Builder.Default
        private RetrievedData retrievedData = null;

        @Builder.Default
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
