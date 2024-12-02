package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class PlotServiceTest {

    @Autowired
    private PlotService plotService;

    @ParameterizedTest
    @MethodSource("replaceCredentialsDataProvider")
    @DisplayName("Replace credentials in the script")
    void testReplaceCredentialsInScript(String inputScript, Database database, String expectedScript, String fileName) {
        String actualScript = plotService.replaceCredentialsInScript(inputScript, database, fileName);
        assertEquals(expectedScript, actualScript);
    }

    static Object[][] replaceCredentialsDataProvider() {
        return new Object[][] {
                {
                        FileUtils.getFileContent("./src/test/resources/llmResponses/plotSexOfUsersSuccess.json"),
                        Database.builder()
                                .host("localhost")
                                .port(5432)
                                .database("myEshop")
                                .userName("jan")
                                .password("secret111")
                                .build(),
                        FileUtils.getFileContent(
                                "./src/test/resources/llmResponses/plotSexOfUsersWithCredentials1.json"),
                        "d9223610-04b5-49e1-8b4e-7b3aeac8836a-0--84728494-04b5-49e1-8b4e-7b3aeac8836a-0.png"
                },
                {
                        FileUtils.getFileContent("./src/test/resources/llmResponses/plotSexOfUsersSuccess.json"),
                        Database.builder()
                                .host("https://my-eshop.com")
                                .port(5432)
                                .database("myEshop")
                                .userName("jan")
                                .password("secret111")
                                .build(),
                        FileUtils.getFileContent(
                                "./src/test/resources/llmResponses/plotSexOfUsersWithCredentials2.json"),
                        "d9223610-04b5-49e1-8b4e-7b3aeac8836a-0--12345678-04b5-49e1-8b4e-7b3aeac8836a-0.png"
                }
        };
    }
}