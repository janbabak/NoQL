package com.janbabak.noqlbackend.service.langChain;

import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.query.RetrievedData;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssistantTools {

    private final ExperimentalQueryService queryService;

    @SuppressWarnings("FieldMayBeFinal")
    private Database database;
    private final int page;
    private final int pageSize;

    @Getter
    private RetrievedData retrievedData;

    public AssistantTools(Database database,
                          int page,
                          int pageSize,
                          ExperimentalQueryService queryService) {
        this.database = database;
        this.page = page;
        this.pageSize = pageSize;
        this.queryService = queryService;
    }

    @Getter
    @AllArgsConstructor
    public static class ToolResult {
        private boolean success;
        private String data;
        private String error;
    }

    @Tool("Execute query on database")
    public ToolResult executeSqlQuery(@P("Query to execute - must use valid database query language") String query) {
        try {
            retrievedData = queryService.executeQuery(query, database, page, pageSize);
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage());
            return new ToolResult(false, null, e.getMessage());
        }
        log.info("Query executed successfully: {}", query);
        return new ToolResult(true, "Query executed successfully", null);
    }

    ;

    @Tool("Generate plot from data")
    public ToolResult generatePlot(@P("Data to plot in CSV format") String pythonCode) {
        log.error("Not implemented yet: generatePlot: {}", pythonCode);
        return new ToolResult(true, null, "Plot successfully generated");
    }
}
