package com.janbabak.noqlbackend.service.chat;

import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.service.database.MessageDataDAO;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatQueryWithResponseServiceTest {

    @InjectMocks
    private ChatQueryWithResponseService chatQueryWithResponseService;

    @Mock
    private ChatQueryWithResponseRepository chatQueryWithResponseRepositoryMock;

    @Mock
    private ChatRepository chatRepositoryMock;

    @Mock
    @SuppressWarnings("unused") // used internally
    AuthenticationService authenticationServiceMock;

    @Mock
    @SuppressWarnings("unused") // used internally
    MessageDataDAO messageDataDAOMock;

    private final Database postgresDatabase;

    public ChatQueryWithResponseServiceTest() {
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
    @DisplayName("Test getMessagesFromChat")
    void getMessagesFromChat() throws EntityNotFoundException {
        // given
        UUID chatId = UUID.randomUUID();

        Chat chat = Chat.builder()
                .id(chatId)
                .name("Test chat")
                .build();

        List<ChatQueryWithResponse> chatQueryWithResponses = List.of(
                ChatQueryWithResponse.builder()
                        .id(UUID.randomUUID())
                        .chat(chat)
                        .nlQuery("Find user Jan")
                        // language=JSON
                        .llmResponse("""
                                { "databaseQuery": "find user Jan", "generatePlot": false, "pythonCode": null }""")
                        .build(),
                ChatQueryWithResponse.builder()
                        .id(UUID.randomUUID())
                        .chat(chat)
                        .nlQuery("Find user Jana")
                        // language=JSON
                        .llmResponse("""
                                { "databaseQuery": "find user Jana", "generatePlot": false, "pythonCode": null }""")
                        .build());

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.of(chat));
        when(chatQueryWithResponseRepositoryMock.findAllByChatOrderByTimestamp(chat))
                .thenReturn(chatQueryWithResponses);

        // when
        List<ChatQueryWithResponse> actual = chatQueryWithResponseService.getMessagesFromChat(chatId);

        // then
        assertEquals(chatQueryWithResponses, actual);
    }

    @Test
    @DisplayName("Test getMessagesFromChat chat not found")
    void getMessagesFromChatChatNotFound() {
        // given
        UUID chatId = UUID.randomUUID();

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> chatQueryWithResponseService.getMessagesFromChat(chatId));

        // then
        assertEquals("Chat of id: \"" + chatId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Test load message data - message not found")
    void testGetDataByMessageIdNotFound() {
        // given
        UUID messageId = UUID.randomUUID();
        int page = 0;
        String expectedErrorMsg = "Message of id: \"" + messageId + "\" not found.";

        when(chatQueryWithResponseRepositoryMock.findById(messageId)).thenReturn(Optional.empty());

        // then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> chatQueryWithResponseService.getDataByMessageId(messageId, page, 10));

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

        when(chatQueryWithResponseRepositoryMock.findById(messageId)).thenReturn(Optional.of(chatQueryWithResponse));

        // then
        assertNull(chatQueryWithResponseService.getDataByMessageId(messageId, 0, 10));
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