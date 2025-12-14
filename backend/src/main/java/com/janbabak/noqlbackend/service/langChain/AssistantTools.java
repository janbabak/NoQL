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
    private QueryResult result;

    @Getter
    @AllArgsConstructor
    public static class QueryResult {
        private Boolean successful;

        private String message;
    }

    @Tool("report system error")
    public QueryResult reportError(
            @P("error to report") String error,
            @P("if the error occurred for the first time") Boolean firstTime) {

        log.info("AI tool: Reporting error: {}, first time: {}", error, firstTime);

        result = new QueryResult(true, "error reported successfully");
        return result;
    }

    @Tool("organize data")
    public QueryResult turnOffUnusedApps() {
        log.info("AI: tool: turn off unused apps");
        result = new QueryResult(true, "turn off unused apps executed successfully");
        return result;
    }
}
