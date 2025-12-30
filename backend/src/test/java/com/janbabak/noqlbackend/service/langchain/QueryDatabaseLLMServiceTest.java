package com.janbabak.noqlbackend.service.langchain;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.service.PlotService;
import com.janbabak.noqlbackend.service.query.QueryExecutionService;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class QueryDatabaseLLMServiceTest {

    @SpyBean
    private QueryDatabaseLLMService queryDatabaseLLMServiceSpy;

    @MockBean
    private QueryExecutionService queryExecutionService;

    @MockBean
    private PlotService plotService;

    /* default */ private final QueryDatabaseLLMService.LLMServiceRequest request =
            QueryDatabaseLLMService.LLMServiceRequest.builder()
                    .userQuery("Find emails of all users")
                    .systemQuery("You are an expert SQL assistant.")
                    .modelId("gpt-4o-mini")
                    .chatHistory(List.of(
                            ChatQueryWithResponse.builder()
                                    .nlQuery("Plot sex of users")
                                    .plotScript("import matplotlib.pyplot as plt; ...")
                                    .plotGenerationSuccess(false)
                                    .plotGenerationErrorMessage("Matplotlib is not installed.")
                                    .resultDescription("Could not generate the plot due to an error.")
                                    .build(),
                            ChatQueryWithResponse.builder()
                                    .nlQuery("Find all users")
                                    .dbQuery("SELECT * FROM users;")
                                    .dbQueryExecutionSuccess(true)
                                    .resultDescription("Found these users.")
                                    .build()))
                    .build();

    @Test
    @DisplayName("Test execute user request")
    @SneakyThrows
    void testExecuteUserRequest() {
        final Assistant assistantMock = mock(Assistant.class);
        when(assistantMock.chat(anyList()))
                .thenReturn("Mocked LLM response");

        doReturn(mock(ChatModel.class))
                .when(queryDatabaseLLMServiceSpy)
                .getModel(anyString());

        doReturn(assistantMock)
                .when(queryDatabaseLLMServiceSpy)
                .buildAssistant(anyString(), any());

        // when
        final QueryDatabaseLLMService.LLMServiceResult result =
                queryDatabaseLLMServiceSpy.executeUserRequest(request);

        // then
        assertEquals("Mocked LLM response", result.llmResponse());
        assertNotNull(result.toolResult());

        // Optionally verify that chat() was called with the correct messages
        verify(assistantMock, times(1)).chat(anyList());
    }

    @Test
    @DisplayName("Test build Messages")
    @SneakyThrows
    @SuppressWarnings("unchecked")
    void testBuildMessages() {
        // given
        final Method method = QueryDatabaseLLMService.class.getDeclaredMethod(
                "buildMessages", QueryDatabaseLLMService.LLMServiceRequest.class);
        method.setAccessible(true);

        final List<ChatMessage> expectedMessages = List.of(
                SystemMessage.from("You are an expert SQL assistant."),
                UserMessage.from("Plot sex of users"),
                AiMessage.builder()
                        .text("Could not generate the plot due to an error.")
                        .toolExecutionRequests(List.of(
                                ToolExecutionRequest.builder()
                                        .name("generatePlot")
                                        .arguments("import matplotlib.pyplot as plt; ...")
                                        .build()))
                        .attributes(mapOf(
                                "generatePlotSuccess", false,
                                "generatePlotError", "Matplotlib is not installed."))
                        .build(),
                UserMessage.from("Find all users"),
                AiMessage.builder()
                        .text("Found these users.")
                        .toolExecutionRequests(List.of(
                                ToolExecutionRequest.builder()
                                        .name("executeQuery")
                                        .arguments("SELECT * FROM users;")
                                        .build()))
                        .attributes(mapOf("executeQuerySuccess", true))
                        .build(),
                UserMessage.from("Find emails of all users")
        );

        // when
        final List<ChatMessage> actualMessages = (List<ChatMessage>) method.invoke(queryDatabaseLLMServiceSpy, request);

        // then
        assertEquals(expectedMessages, actualMessages);
    }

    @ParameterizedTest
    @SneakyThrows
    @DisplayName("Test build LLM response message method")
    @MethodSource("testBuildLLMResponseMessageData")
    void testBuildLLMResponseMessage(ChatQueryWithResponse chatEntry, AiMessage expected) {
        // given
        final Method method = QueryDatabaseLLMService.class.getDeclaredMethod(
                "buildAiMessage", ChatQueryWithResponse.class);
        method.setAccessible(true);

        // when
        final AiMessage actual = (AiMessage) method.invoke(queryDatabaseLLMServiceSpy, chatEntry);

        // theen
        assertEquals(expected, actual);
    }

    static Object[][] testBuildLLMResponseMessageData() {
        return new Object[][]{
                // Database query successfully executed
                {
                        ChatQueryWithResponse.builder()
                                .nlQuery("Find all users")
                                .dbQuery("SELECT * FROM users;")
                                .dbQueryExecutionSuccess(true)
                                .resultDescription("Found these users.")
                                .build(),
                        AiMessage.builder()
                                .text("Found these users.")
                                .toolExecutionRequests(List.of(
                                        ToolExecutionRequest.builder()
                                                .name("executeQuery")
                                                .arguments("SELECT * FROM users;")
                                                .build()))
                                .attributes(mapOf("executeQuerySuccess", true))
                                .build()
                },
                // Database query failed
                {
                        ChatQueryWithResponse.builder()
                                .nlQuery("Find all users")
                                .dbQuery("SELECT * FROM user;")
                                .dbQueryExecutionSuccess(false)
                                .dbExecutionErrorMessage("Table 'user' does not exist.")
                                .resultDescription("Could not find users due to an error.")
                                .build(),
                        AiMessage.builder()
                                .text("Could not find users due to an error.")
                                .toolExecutionRequests(List.of(
                                        ToolExecutionRequest.builder()
                                                .name("executeQuery")
                                                .arguments("SELECT * FROM user;")
                                                .build()))
                                .attributes(mapOf(
                                        "executeQuerySuccess", false,
                                        "executeQueryError", "Table 'user' does not exist."))
                                .build()
                },
                // Plot successfully generated
                {
                        ChatQueryWithResponse.builder()
                                .nlQuery("Plot sex of users")
                                .plotScript("import matplotlib.pyplot as plt; ...")
                                .plotGenerationSuccess(true)
                                .resultDescription("Generated the plot successfully.")
                                .build(),
                        AiMessage.builder()
                                .text("Generated the plot successfully.")
                                .toolExecutionRequests(List.of(
                                        ToolExecutionRequest.builder()
                                                .name("generatePlot")
                                                .arguments("import matplotlib.pyplot as plt; ...")
                                                .build()))
                                .attributes(mapOf("generatePlotSuccess", true))
                                .build()
                },
                // Plot generation failed
                {
                        ChatQueryWithResponse.builder()
                                .nlQuery("Plot sex of users")
                                .plotScript("import matplotlib.pyplot as plt; ...")
                                .plotGenerationSuccess(false)
                                .plotGenerationErrorMessage("Matplotlib is not installed.")
                                .resultDescription("Could not generate the plot due to an error.")
                                .build(),
                        AiMessage.builder()
                                .text("Could not generate the plot due to an error.")
                                .toolExecutionRequests(List.of(
                                        ToolExecutionRequest.builder()
                                                .name("generatePlot")
                                                .arguments("import matplotlib.pyplot as plt; ...")
                                                .build()))
                                .attributes(mapOf(
                                        "generatePlotSuccess", false,
                                        "generatePlotError", "Matplotlib is not installed."))
                                .build()
                },
                // Database query and plot generation successfully executed
                {
                        ChatQueryWithResponse.builder()
                                .nlQuery("Plot sex of users")
                                .dbQuery("SELECT sex, COUNT(*) FROM users GROUP BY sex;")
                                .dbQueryExecutionSuccess(true)
                                .plotScript("import matplotlib.pyplot as plt; ...")
                                .plotGenerationSuccess(true)
                                .resultDescription("Generated the plot successfully.")
                                .build(),
                        AiMessage.builder()
                                .text("Generated the plot successfully.")
                                .toolExecutionRequests(List.of(
                                        ToolExecutionRequest.builder()
                                                .name("executeQuery")
                                                .arguments("SELECT sex, COUNT(*) FROM users GROUP BY sex;")
                                                .build(),
                                        ToolExecutionRequest.builder()
                                                .name("generatePlot")
                                                .arguments("import matplotlib.pyplot as plt; ...")
                                                .build()))
                                .attributes(mapOf(
                                        "executeQuerySuccess", true,
                                        "generatePlotSuccess", true))
                                .build()
                },
                // Database query and plot generation failed
                {
                        ChatQueryWithResponse.builder()
                                .nlQuery("Plot sex of users")
                                .dbQuery("SELECT sex, COUNT(*) FROM user GROUP BY sex;")
                                .dbQueryExecutionSuccess(false)
                                .dbExecutionErrorMessage("Table 'user' does not exist.")
                                .plotScript("import matplotlib.pyplot as plt; ...")
                                .plotGenerationSuccess(false)
                                .plotGenerationErrorMessage("Matplotlib is not installed.")
                                .resultDescription("Could not generate the plot due to errors.")
                                .build(),
                        AiMessage.builder()
                                .text("Could not generate the plot due to errors.")
                                .toolExecutionRequests(List.of(
                                        ToolExecutionRequest.builder()
                                                .name("executeQuery")
                                                .arguments("SELECT sex, COUNT(*) FROM user GROUP BY sex;")
                                                .build(),
                                        ToolExecutionRequest.builder()
                                                .name("generatePlot")
                                                .arguments("import matplotlib.pyplot as plt; ...")
                                                .build()))
                                .attributes(mapOf(
                                        "executeQuerySuccess", false,
                                        "executeQueryError", "Table 'user' does not exist.",
                                        "generatePlotSuccess", false,
                                        "generatePlotError", "Matplotlib is not installed."))
                                .build()
                },
                // Database query successfully executed and plot generation failed
                {
                        ChatQueryWithResponse.builder()
                                .nlQuery("Plot sex of users")
                                .dbQuery("SELECT sex, COUNT(*) FROM users GROUP BY sex;")
                                .dbQueryExecutionSuccess(true)
                                .plotScript("import matplotlib.pyplot as plt; ...")
                                .plotGenerationSuccess(false)
                                .plotGenerationErrorMessage("Matplotlib is not installed.")
                                .resultDescription("Could not generate the plot due to an error.")
                                .build(),
                        AiMessage.builder()
                                .text("Could not generate the plot due to an error.")
                                .toolExecutionRequests(List.of(
                                        ToolExecutionRequest.builder()
                                                .name("executeQuery")
                                                .arguments("SELECT sex, COUNT(*) FROM users GROUP BY sex;")
                                                .build(),
                                        ToolExecutionRequest.builder()
                                                .name("generatePlot")
                                                .arguments("import matplotlib.pyplot as plt; ...")
                                                .build()))
                                .attributes(mapOf(
                                        "executeQuerySuccess", true,
                                        "generatePlotSuccess", false,
                                        "generatePlotError", "Matplotlib is not installed."))
                                .build()
                },
                // Database query execution failed and plot successfully generated
                {
                        ChatQueryWithResponse.builder()
                                .nlQuery("Plot sex of users")
                                .dbQuery("SELECT sex, COUNT(*) FROM user GROUP BY sex;")
                                .dbQueryExecutionSuccess(false)
                                .dbExecutionErrorMessage("Table 'user' does not exist.")
                                .plotScript("import matplotlib.pyplot as plt; ...")
                                .plotGenerationSuccess(true)
                                .resultDescription("Generated the plot successfully.")
                                .build(),
                        AiMessage.builder()
                                .text("Generated the plot successfully.")
                                .toolExecutionRequests(List.of(
                                        ToolExecutionRequest.builder()
                                                .name("executeQuery")
                                                .arguments("SELECT sex, COUNT(*) FROM user GROUP BY sex;")
                                                .build(),
                                        ToolExecutionRequest.builder()
                                                .name("generatePlot")
                                                .arguments("import matplotlib.pyplot as plt; ...")
                                                .build()))
                                .attributes(mapOf(
                                        "executeQuerySuccess", false,
                                        "executeQueryError", "Table 'user' does not exist.",
                                        "generatePlotSuccess", true))
                                .build()
                }
        };
    }

    /**
     * @param keyValues String1, Object1, String2, Object2, ...
     */
    private static Map<String, Object> mapOf(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide an even number of arguments (key-value pairs).");
        }

        final Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            final String key = (String) keyValues[i];
            final Object value = keyValues[i + 1];
            map.put(key, value);
        }
        return map;
    }
}