package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.database.DatabaseCredentialsEncryptionService;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlotServiceTest {

    @Autowired
    @SuppressWarnings("unused")
    private PlotService plotService;

    @Autowired
    @SuppressWarnings("unused")
    private DatabaseCredentialsEncryptionService encryptionService;

    @ParameterizedTest
    @MethodSource("replaceCredentialsDataProvider")
    @DisplayName("Replace credentials in the script")
    void testReplaceCredentialsInScript(String inputScript, Database database, String expectedScript, String fileName) {
        String actualScript = plotService.replaceCredentialsInScript(inputScript, database, fileName);
        assertEquals(expectedScript, actualScript);
    }

    Object[][] replaceCredentialsDataProvider() {
        return new Object[][] {
                {
                        FileUtils.getFileContent("./src/test/resources/llmResponses/plotSexOfUsersSuccess.py"),
                        Database.builder()
                                .host("localhost")
                                .port(5432)
                                .database("myEshop")
                                .userName("jan")
                                .password(encryptionService.encryptCredentials("secret111"))
                                .build(),
                        FileUtils.getFileContent(
                                "./src/test/resources/llmResponses/plotSexOfUsersWithCredentials1.py"),
                        "68712114-b7b4-451a-a6eb-1c6e822509ae--12345678-b7b4-451a-a6eb-1c6e822509ae.png"
                },
                {
                        FileUtils.getFileContent("./src/test/resources/llmResponses/plotSexOfUsersSuccess.py"),
                        Database.builder()
                                .host("https://my-eshop.com")
                                .port(5432)
                                .database("myEshop")
                                .userName("jan")
                                .password(encryptionService.encryptCredentials("secret111"))
                                .build(),
                        FileUtils.getFileContent(
                                "./src/test/resources/llmResponses/plotSexOfUsersWithCredentials2.py"),
                        "68712114-b7b4-451a-a6eb-1c6e822509ae--12345678-b7b4-451a-a6eb-1c6e822509ae.png"
                }
        };
    }

    @Test
    @DisplayName("Create file name")
    void testCreateFileName() {
        // given
        String chatId = "68712114-b7b4-451a-a6eb-1c6e822509ae";
        String messageId = "12345678-b7b4-451a-a6eb-1c6e822509ae";
        String expectedFileName = "68712114-b7b4-451a-a6eb-1c6e822509ae--12345678-b7b4-451a-a6eb-1c6e822509ae.png";

        // when
        String actualFileName = PlotService.createFileName(UUID.fromString(chatId), UUID.fromString(messageId));

        // then
        assertEquals(expectedFileName, actualFileName);
    }

    @Test
    @DisplayName("Create file URL")
    void testCreateFileUrl() {
        // given
        String fileName = "68712114-b7b4-451a-a6eb-1c6e822509ae--12345678-b7b4-451a-a6eb-1c6e822509ae.png";
        String expectedUrl = "/static/images/68712114-b7b4-451a-a6eb-1c6e822509ae--12345678-b7b4-451a-a6eb-1c6e822509ae.png";

        // when
        String actualFileUrl = PlotService.createFileUrl(fileName);

        // then
        assertEquals(expectedUrl, actualFileUrl);
    }

    @Test
    @DisplayName("Create file URL from IDs")
    void testCreateFileUrlFromIds() {
        // given
        String chatId = "68712114-b7b4-451a-a6eb-1c6e822509ae";
        String messageId = "12345678-b7b4-451a-a6eb-1c6e822509ae";
        String expectedUrl = "/static/images/68712114-b7b4-451a-a6eb-1c6e822509ae--12345678-b7b4-451a-a6eb-1c6e822509ae.png";

        // when
        String actualFileUrl = PlotService.createFileUrl(UUID.fromString(chatId), UUID.fromString(messageId));

        // then
        assertEquals(expectedUrl, actualFileUrl);
    }
}