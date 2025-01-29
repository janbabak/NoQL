package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class MessageDataDAOTest {

    @InjectMocks
    private MessageDataDAO messageDataDAO;

    @Mock
    private ChatQueryWithResponseRepository chatQueryWithResponseRepository;

    @Mock
    @SuppressWarnings("unused") // used internally
    AuthenticationService authenticationService;

    private final Database postgresDatabase;

    public MessageDataDAOTest() {
        User testUser = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("test@gmail.com")
                .password("password")
                .build();
        postgresDatabase = new Database(
                UUID.randomUUID(),
                "Postgres db",
                "localhost",
                5432,
                "database",
                "jan",
                "4530958340??",
                DatabaseEngine.POSTGRES,
                List.of(),
                testUser);
    }

    @Test
    @DisplayName("Test load message data - message not found")
    void testGetDataByMessageIdNotFound() {
        // given
        UUID messageId = UUID.randomUUID();
        int page = 0;
        String expectedErrorMsg = "Message of id: \"" + messageId + "\" not found.";

        when(chatQueryWithResponseRepository.findById(messageId)).thenReturn(Optional.empty());

        // then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> messageDataDAO.getDataByMessageId(messageId, page, 10));

        assertEquals(expectedErrorMsg, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("testGetDataByMessageIdLlmResponseHasEmptyQueryTestDataProvider")
    @DisplayName("Test load message data - LLM response has empty query")
    void testGetDataByMessageIdLlmResponseHasEmptyQueryTest(String llmResponse) throws EntityNotFoundException {

        // given
        UUID messageId = UUID.randomUUID();

        ChatQueryWithResponse chatQueryWithResponse = ChatQueryWithResponse.builder()
                .id(messageId)
                .chat(Chat.builder()
                        .id(UUID.randomUUID())
                        .database(postgresDatabase)
                        .build())
                .llmResponse(llmResponse)
                .build();

        when(chatQueryWithResponseRepository.findById(messageId)).thenReturn(Optional.of(chatQueryWithResponse));

        // then
        assertNull(messageDataDAO.getDataByMessageId(messageId, 0, 10));
    }

    static Object[][] testGetDataByMessageIdLlmResponseHasEmptyQueryTestDataProvider() {
        return new Object[][]{
                {
                        null
                },
                {
                        ""
                },
                {
                        "{}"
                },
                {
                        // language=JSON
                        """
                                {
                                  "databaseQuery": "",
                                  "generatePlot": false,
                                  "pythonCode": ""
                                }"""
                }
        };
    }

}