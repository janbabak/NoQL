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
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import com.janbabak.noqlbackend.service.PlotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Mock
    @SuppressWarnings("unused") // used in the ChatService
    private AuthenticationService authenticationService;

    private final User testUser = User.builder()
            .id(UUID.randomUUID())
            .build();

    private final User testUser2 = User.builder()
            .id(UUID.randomUUID())
            .build();

    @Test
    @DisplayName("Test find chat by id")
    void testFindChatById() throws EntityNotFoundException {
        // given
        UUID chatId = UUID.randomUUID();

        Chat chat = Chat.builder()
                .id(chatId)
                .name("Test chat")
                .messages(new ArrayList<>())
                .database(Database.builder().user(testUser).build())
                .build();

        ChatDto expected = new ChatDto(
                chatId, "Test chat", new ArrayList<>(), null, null);

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        // when
        ChatDto actual = chatService.findById(chatId, null, true);

        // then
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(chatRepository).findById(idCaptor.capture());
        assertEquals(chatId, idCaptor.getValue());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test find chat by id - user is not owner of the chat")
    void testFindChatByIdUserIsNotOwner() {
        // given
        UUID chatId = UUID.randomUUID();

        Chat chat = Chat.builder()
                .id(chatId)
                .name("Test chat")
                .messages(new ArrayList<>())
                .database(Database.builder().user(testUser2).build())
                .build();

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationService).ifNotAdminOrSelfRequestThrowAccessDenied(testUser2.getId());

        // then
        assertThrows(AccessDeniedException.class,
                () -> chatService.findById(chatId, null, true));
    }

    @Test
    @DisplayName("Test find chat by id not found")
    void testFindChatByIdNotFound() {
        // given
        UUID chatId = UUID.randomUUID();

        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> chatService.findById(chatId, null, true));

        // then
        assertEquals("Chat of id: \"" + chatId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Test find chats by database id")
    void testFindChatsByDatabaseId() throws EntityNotFoundException {
        // given
        UUID databaseId = UUID.randomUUID();

        Database database = Database.builder()
                .id(databaseId)
                .name("Test database")
                .user(testUser)
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

        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(database));
        when(chatRepository.findAllByDatabaseOrderByModificationDateDesc(database)).thenReturn(chats);

        // when
        List<ChatHistoryItem> actual = chatService.findChatsByDatabaseId(databaseId);

        // then
        ArgumentCaptor<Database> databaseArgumentCaptor = ArgumentCaptor.forClass(Database.class);
        verify(chatRepository).findAllByDatabaseOrderByModificationDateDesc(databaseArgumentCaptor.capture());
        assertEquals(database, databaseArgumentCaptor.getValue());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test find chats by database id - user is not database owner")
    void testFindChatsByDatabaseIdUserIsNotOwner() {
        // given
        UUID databaseId = UUID.randomUUID();

        Database database = Database.builder()
                .id(databaseId)
                .name("Test database")
                .user(testUser2)
                .build();

        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationService).ifNotAdminOrSelfRequestThrowAccessDenied(testUser2.getId());
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(database));

        // then
        assertThrows(AccessDeniedException.class, () -> chatService.findChatsByDatabaseId(databaseId));
    }

    @Test
    @DisplayName("Test find chats by database id not found")
    void testFindChatsByDatabaseIdNotFound() {
        // given
        UUID databaseId = UUID.randomUUID();

        when(databaseRepository.findById(databaseId)).thenReturn(Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> chatService.findChatsByDatabaseId(databaseId));

        // then
        assertEquals("Database of id: \"" + databaseId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Test create chat")
    void testCreate() throws EntityNotFoundException {
        // given
        UUID databaseId = UUID.randomUUID();

        Database database = Database.builder()
                .id(databaseId)
                .name("Test database")
                .user(testUser)
                .build();

        Chat chat = Chat.builder()
                .id(UUID.randomUUID())
                .name("New chat")
                .messages(new ArrayList<>())
                .database(database)
                .build();

        ChatDto expected = new ChatDto(
                chat.getId(), "New chat", new ArrayList<>(), null, databaseId);

        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(database));
        when(chatRepository.save(any())).thenReturn(chat);

        // when
        ChatDto actual = chatService.create(databaseId);

        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test create chat - user is not owner of the database")
    void testCreateUserIsNotOwner() {
        // given
        UUID databaseId = UUID.randomUUID();

        Database database = Database.builder()
                .id(databaseId)
                .name("Test database")
                .user(testUser2)
                .build();

        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(database));
        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationService).ifNotAdminOrSelfRequestThrowAccessDenied(testUser2.getId());

        // then
        assertThrows(AccessDeniedException.class, () -> chatService.create(databaseId));
    }

    @Test
    @DisplayName("Test create chat database not found")
    void testCreateDatabaseNotFound() {
        // given
        UUID databaseId = UUID.randomUUID();

        when(databaseRepository.findById(databaseId)).thenReturn(Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> chatService.create(databaseId));

        // then
        assertEquals("Database of id: \"" + databaseId + "\" not found.", exception.getMessage());
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
                .database(Database.builder().user(testUser).build())
                .build();

        CreateChatQueryWithResponseRequest request = new CreateChatQueryWithResponseRequest(
                "Visualize age of users",
                // language=JSON
                """
                        { "databaseQuery": "string", "generatePlot": true, "pythonCode": "import something etc." }""");

        ChatQueryWithResponse expected = ChatQueryWithResponse.builder()
                .id(UUID.randomUUID())
                .chat(chat)
                .nlQuery(request.nlQuery())
                .llmResponse(request.llmResult())
                .build();

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(messageRepository.save(any())).thenReturn(expected);

        // when
        ChatQueryWithResponse actual = chatService.addMessageToChat(chatId, request);

        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test add message to chat - user is not owner of the chat")
    void testAddMessageToChatUserIsNotOwner() {
        // given
        UUID chatId = UUID.randomUUID();

        Chat chat = Chat.builder()
                .id(chatId)
                .name("Visualize age of users")
                .messages(new ArrayList<>())
                .database(Database.builder().user(testUser2).build())
                .build();

        CreateChatQueryWithResponseRequest request = new CreateChatQueryWithResponseRequest(
                "Visualize age of users",
                // language=JSON
                """
                        { "databaseQuery": "string", "generatePlot": true, "pythonCode": "import something etc." }""");

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationService).ifNotAdminOrSelfRequestThrowAccessDenied(testUser2.getId());
        // then
        assertThrows(AccessDeniedException.class, () -> chatService.addMessageToChat(chatId, request));
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
                .database(Database.builder().user(testUser).build())
                .build();

        CreateChatQueryWithResponseRequest request = new CreateChatQueryWithResponseRequest(
                "Create a histogram of distribution of age of users",
                // language=JSON
                """
                        { "databaseQuery": "string", "generatePlot": true, "pythonCode": "import something etc." }""");

        ChatQueryWithResponse expected = ChatQueryWithResponse.builder()
                .id(UUID.randomUUID())
                .chat(chat)
                .nlQuery(request.nlQuery())
                .llmResponse(request.llmResult())
                .build();

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(messageRepository.save(any())).thenReturn(expected);

        // when
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

        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> chatService.addMessageToChat(chatId, request));

        // then
        assertEquals("Chat of id: \"" + chatId + "\" not found.", exception.getMessage());
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
                .database(Database.builder().user(testUser).build())
                .build();

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        // when
        chatService.renameChat(chatId, newName);

        // then
        ArgumentCaptor<Chat> chatCaptor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(chatCaptor.capture());
        assertEquals(newName, chatCaptor.getValue().getName());
    }

    @Test
    @DisplayName("Test rename chat - user is not owner of the chat")
    void testRenameChatUserIsNotOwner() {
        // given
        UUID chatId = UUID.randomUUID();

        String newName = "User's age distribution";

        Chat chat = Chat.builder()
                .id(chatId)
                .name("Visualize age of users")
                .messages(new ArrayList<>())
                .database(Database.builder().user(testUser2).build())
                .build();

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationService).ifNotAdminOrSelfRequestThrowAccessDenied(testUser2.getId());

        // then
        assertThrows(AccessDeniedException.class, () -> chatService.renameChat(chatId, newName));
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
                .database(Database.builder().user(testUser).build())
                .build();

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        // when
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

        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> chatService.renameChat(chatId, newName));

        // then
        assertEquals("Chat of id: \"" + chatId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Test delete chat by id")
    void testDeleteChatById() {
        // given
        UUID chatId = UUID.randomUUID();

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(Chat.builder()
                .id(chatId)
                .database(Database.builder().user(testUser).build())
                .build()));

        // when
        chatService.deleteChatById(chatId);

        // then
        ArgumentCaptor<UUID> repositoryIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> plotIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatRepository).deleteById(repositoryIdCaptor.capture());
        verify(plotService).deletePlots(plotIdCaptor.capture());
        assertEquals(chatId, repositoryIdCaptor.getValue());
        assertEquals(chatId.toString(), plotIdCaptor.getValue());
    }

    @Test
    @DisplayName("Test delete chat by id - user is not owner of the chat")
    void testDeleteChatByIdUserIsNotOwner() {
        // given
        UUID chatId = UUID.randomUUID();

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(Chat.builder()
                .id(chatId)
                .database(Database.builder().user(testUser2).build())
                .build()));
        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationService).ifNotAdminOrSelfRequestThrowAccessDenied(testUser2.getId());

        // then
        assertThrows(AccessDeniedException.class, () -> chatService.deleteChatById(chatId));
    }
}