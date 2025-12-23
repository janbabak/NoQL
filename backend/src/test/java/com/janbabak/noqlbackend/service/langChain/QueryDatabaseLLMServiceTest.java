package com.janbabak.noqlbackend.service.langChain;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QueryDatabaseLLMServiceTest {

    @Autowired
    private QueryDatabaseLLMService queryDatabaseLLMService;

    @ParameterizedTest
    @SneakyThrows
    @DisplayName("Test build LLM response message method")
    @MethodSource("testBuildLLMResponseMessageData")
    void testBuildLLMResponseMessage(ChatQueryWithResponse chatEntry, String expected) {
        Method method = QueryDatabaseLLMService.class.getDeclaredMethod(
                "buildLLMResponseMessage", ChatQueryWithResponse.class);
        method.setAccessible(true);
        String actual = (String) method.invoke(queryDatabaseLLMService, chatEntry);
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
                        """
                        LLM Response: Found these users.
                        
                        Executed tools:
                        
                        Method call: executeQuery("SELECT * FROM users;")
                        Success: true"""
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
                        """
                        LLM Response: Could not find users due to an error.
                       
                        Executed tools:
                        
                        Method call: executeQuery("SELECT * FROM user;")
                        Success: false
                        Errors: Table 'user' does not exist."""
                },
                // Plot successfully generated
                {
                        ChatQueryWithResponse.builder()
                                .nlQuery("Plot sex of users")
                                .plotScript("import matplotlib.pyplot as plt; ...")
                                .plotGenerationSuccess(true)
                                .resultDescription("Generated the plot successfully.")
                                .build(),
                        """
                        LLM Response: Generated the plot successfully.
                        
                        Executed tools:
                        
                        Method call: generatePlot("import matplotlib.pyplot as plt; ...")
                        Success: true"""
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
                        """
                        LLM Response: Could not generate the plot due to an error.
                        
                        Executed tools:
                        
                        Method call: generatePlot("import matplotlib.pyplot as plt; ...")
                        Success: false
                        Errors: Matplotlib is not installed."""
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
                        """
                        LLM Response: Generated the plot successfully.
                       
                        Executed tools:
                        
                        Method call: executeQuery("SELECT sex, COUNT(*) FROM users GROUP BY sex;")
                        Success: true
                        
                        Method call: generatePlot("import matplotlib.pyplot as plt; ...")
                        Success: true"""
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
                        """
                        LLM Response: Could not generate the plot due to errors.
                        
                        Executed tools:
                        
                        Method call: executeQuery("SELECT sex, COUNT(*) FROM user GROUP BY sex;")
                        Success: false
                        Errors: Table 'user' does not exist.
                        
                        Method call: generatePlot("import matplotlib.pyplot as plt; ...")
                        Success: false
                        Errors: Matplotlib is not installed."""
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
                        """
                        LLM Response: Could not generate the plot due to an error.

                        Executed tools:

                        Method call: executeQuery("SELECT sex, COUNT(*) FROM users GROUP BY sex;")
                        Success: true

                        Method call: generatePlot("import matplotlib.pyplot as plt; ...")
                        Success: false
                        Errors: Matplotlib is not installed."""
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
                        """
                        LLM Response: Generated the plot successfully.
   
                        Executed tools:
                        
                        Method call: executeQuery("SELECT sex, COUNT(*) FROM user GROUP BY sex;")
                        Success: false
                        Errors: Table 'user' does not exist.
                        
                        Method call: generatePlot("import matplotlib.pyplot as plt; ...")
                        Success: true"""
                }
        };
    }
}