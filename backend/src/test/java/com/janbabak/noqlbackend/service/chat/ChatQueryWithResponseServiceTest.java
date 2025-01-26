package com.janbabak.noqlbackend.service.chat;

import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private ChatQueryWithResponseRepository chatQueryWithResponseRepository;

    @Mock
    private ChatRepository chatRepository;

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

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(chatQueryWithResponseRepository.findAllByChatOrderByTimestamp(chat)).thenReturn(chatQueryWithResponses);

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

        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> chatQueryWithResponseService.getMessagesFromChat(chatId));

        // then
        assertEquals("Chat of id: \"" + chatId + "\" not found.", exception.getMessage());
    }
}