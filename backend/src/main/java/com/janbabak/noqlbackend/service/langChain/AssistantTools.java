package com.janbabak.noqlbackend.service.langChain;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AssistantTools {

    @Getter
    @AllArgsConstructor
    public static class QueryResult {
        private Boolean successful;

        private String message;
    }

    @Tool("execute_query")
    public QueryResult executeQuery(
            @P("query in the query language") String query,
            @P("if the language is SQL") Boolean isSql) {

        log.info("Executing {} query: {}", isSql ? "SQL" : "NoSQL", query);

        return new QueryResult(true, "Query executed successfully");
    }

    @Tool("generate plot")
    public QueryResult generatePlot(@P("type of the plot e.g. pie chart, bar chart, line chart, ...") String plotType) {
        log.info("Generating {} plot", plotType);

        return new QueryResult(true, "Plot generated successfully");
    }

    @Tool("display users tasks for current day")
    public QueryResult displayUsersTasks(@P("Number of tasks to display") Integer numberOfTasks) {
        log.info("Displaying {} tasks for the current day", numberOfTasks);
        System.out.println("Displaying " + numberOfTasks + " tasks for the current day");

        return new QueryResult(true, "Tasks displayed successfully");
    }
}
