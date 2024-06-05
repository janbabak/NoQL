package com.janbabak.noqlbackend.service.chat;

import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.ChatHistoryItem;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.PlotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ChatQueryWithResponseRepository messageRepository;

    @Mock
    private DatabaseRepository databaseRepository;

    @Mock
    private PlotService plotService;

    @Test
    @DisplayName("Test find chat by id")
    void testFindChatById() throws EntityNotFoundException {
        // given
        UUID chatId = UUID.randomUUID();

        Chat chat = Chat.builder()
                .id(chatId)
                .name("Test chat")
                .messages(new ArrayList<>())
                .build();

        ChatDto expected = new ChatDto(chatId, "Test chat", new ArrayList<>(), null);

        // when
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        ChatDto actual = chatService.findById(chatId);

        // then
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(chatRepository).findById(idCaptor.capture());
        assertEquals(chatId, idCaptor.getValue());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test find chat by id not found")
    void testFindChatByIdNotFound() {
        // given
        UUID chatId = UUID.randomUUID();

        // when
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> chatService.findById(chatId));
    }

    @Test
    @DisplayName("Test find chats by database id")
    void testFindChatsByDatabaseId() throws EntityNotFoundException {
        // given
        UUID databaseId = UUID.randomUUID();

        Database database = Database.builder()
                .id(databaseId)
                .name("Test database")
                .build();

        Chat chat1 = Chat.builder()
                .id(UUID.randomUUID())
                .name("Find the oldest user")
                .messages(new ArrayList<>())
                .database(database)
                .build();

        Chat chat2 = Chat.builder()
                .id(UUID.randomUUID())
                .name("find emails of all users")
                .messages(new ArrayList<>())
                .database(database)
                .build();

        List<Chat> chats = List.of(chat1, chat2);

        List<ChatHistoryItem> expected = List.of(
                new ChatHistoryItem(chat1.getId(), "Find the oldest user"),
                new ChatHistoryItem(chat2.getId(), "find emails of all users"));

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(database));
        when(chatRepository.findAllByDatabaseOrderByModificationDateDesc(database)).thenReturn(chats);
        List<ChatHistoryItem> actual = chatService.findChatsByDatabaseId(databaseId);

        // then
        ArgumentCaptor<Database> databaseArgumentCaptor = ArgumentCaptor.forClass(Database.class);
        verify(chatRepository).findAllByDatabaseOrderByModificationDateDesc(databaseArgumentCaptor.capture());
        assertEquals(database, databaseArgumentCaptor.getValue());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test find chats by database id not found")
    void testFindChatsByDatabaseIdNotFound() {
        // given
        UUID databaseId = UUID.randomUUID();

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> chatService.findChatsByDatabaseId(databaseId));
    }

    @Test
    @DisplayName("Test create chat")
    void testCreate() throws EntityNotFoundException {
        // given
        UUID databaseId = UUID.randomUUID();

        Database database = Database.builder()
                .id(databaseId)
                .name("Test database")
                .build();

        Chat chat = Chat.builder()
                .id(UUID.randomUUID())
                .name("New chat")
                .messages(new ArrayList<>())
                .database(database)
                .build();

        ChatDto expected = new ChatDto(chat.getId(), "New chat", new ArrayList<>(), null);

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(database));
        when(chatRepository.save(any())).thenReturn(chat);
        ChatDto actual = chatService.create(databaseId);

        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test create chat database not found")
    void testCreateDatabaseNotFound() {
        // given
        UUID databaseId = UUID.randomUUID();

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> chatService.create(databaseId));
    }

    @Test
    @DisplayName("Test add message to chat")
    void testAddMessageToChat() throws EntityNotFoundException {
        // given
        UUID chatId = UUID.randomUUID();

        Chat chat = Chat.builder()
                .id(chatId)
                .name("Visualize age of users")
                .messages(new ArrayList<>())
                .build();

        CreateChatQueryWithResponseRequest request = new CreateChatQueryWithResponseRequest(
                "Visualize age of users",
                // language=JSON
                """
                        { "databaseQuery": "string", "generatePlot": true, "pythonCode": "import something etc." }""");

        ChatQueryWithResponse expected = ChatQueryWithResponse.builder()
                .id(UUID.randomUUID())
                .chat(chat)
                .nlQuery(request.getNlQuery())
                .llmResponse(request.getLlmResult())
                .build();

        // when
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(messageRepository.save(any())).thenReturn(expected);
        ChatQueryWithResponse actual = chatService.addMessageToChat(chatId, request);

        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test add message to new chat and change chat name")
    void testAddMessageToNewChatAndChangeItsName() throws EntityNotFoundException {
        // given
        UUID chatId = UUID.randomUUID();

        Chat chat = Chat.builder()
                .id(chatId)
                .name("New chat")
                .messages(new ArrayList<>())
                .build();

        CreateChatQueryWithResponseRequest request = new CreateChatQueryWithResponseRequest(
                "Create a histogram of distribution of age of users",
                // language=JSON
                """
                        { "databaseQuery": "string", "generatePlot": true, "pythonCode": "import something etc." }""");

        ChatQueryWithResponse expected = ChatQueryWithResponse.builder()
                .id(UUID.randomUUID())
                .chat(chat)
                .nlQuery(request.getNlQuery())
                .llmResponse(request.getLlmResult())
                .build();

        // when
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(messageRepository.save(any())).thenReturn(expected);
        ChatQueryWithResponse actual = chatService.addMessageToChat(chatId, request);

        // then
        ArgumentCaptor<Chat> chatCaptor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(chatCaptor.capture());
        assertEquals("Create a histogram of distributi", chatCaptor.getValue().getName());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test add message to chat not found")
    void testAddMessageToChatNotFound() {
        // given
        UUID chatId = UUID.randomUUID();

        CreateChatQueryWithResponseRequest request = new CreateChatQueryWithResponseRequest(
                "Visualize age of users",
                // language=JSON
                """
                        { "databaseQuery": "string", "generatePlot": true, "pythonCode": "import something etc." }""");

        // when
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> chatService.addMessageToChat(chatId, request));
    }

    @Test
    @DisplayName("Test rename chat")
    void testRenameChat() throws EntityNotFoundException {
        // given
        UUID chatId = UUID.randomUUID();

        String newName = "User's age distribution";

        Chat chat = Chat.builder()
                .id(chatId)
                .name("Visualize age of users")
                .messages(new ArrayList<>())
                .build();

        // when
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        chatService.renameChat(chatId, newName);

        // then
        ArgumentCaptor<Chat> chatCaptor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(chatCaptor.capture());
        assertEquals(newName, chatCaptor.getValue().getName());
    }

    @Test
    @DisplayName("Test rename chat name longer than limit")
    void testRenameChatLongName() throws EntityNotFoundException {
        // given
        UUID chatId = UUID.randomUUID();

        String newName = "Histogram of user's age distribution";

        Chat chat = Chat.builder()
                .id(chatId)
                .name("Visualize age of users")
                .messages(new ArrayList<>())
                .build();

        // when
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        chatService.renameChat(chatId, newName);

        // then
        ArgumentCaptor<Chat> chatCaptor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(chatCaptor.capture());
        assertEquals("Histogram of user's age distribu", chatCaptor.getValue().getName());
    }

    @Test
    @DisplayName("Test rename chat not found")
    void testRenameChatNotFound() {
        // given
        UUID chatId = UUID.randomUUID();

        String newName = "User's age distribution";

        // when
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> chatService.renameChat(chatId, newName));
    }

    @Test
    @DisplayName("Test delete chat by id")
    void testDeleteChatById() {
        // given
        UUID chatId = UUID.randomUUID();

        // when
        chatService.deleteChatById(chatId);

        // then
        ArgumentCaptor<UUID> repositoryIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UUID> plotIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(chatRepository).deleteById(repositoryIdCaptor.capture());
        verify(plotService).deletePlot(plotIdCaptor.capture());
        assertEquals(chatId, repositoryIdCaptor.getValue());
        assertEquals(chatId, plotIdCaptor.getValue());
    }
}