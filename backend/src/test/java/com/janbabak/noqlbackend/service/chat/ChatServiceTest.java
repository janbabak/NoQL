package com.janbabak.noqlbackend.service.chat;

import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.ChatHistoryItem;
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
    private ChatRepository chatRepositoryMock;

    @Mock
    private ChatQueryWithResponseRepository messageRepositoryMock;

    @Mock
    private DatabaseRepository databaseRepositoryMock;

    @Mock
    private PlotService plotServiceMock;

    @Mock
    private AuthenticationService authenticationServiceMock;

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
        final UUID chatId = UUID.randomUUID();

        final Chat chat = Chat.builder()
                .id(chatId)
                .name("Test chat")
                .messages(new ArrayList<>())
                .database(Database.builder().user(testUser).build())
                .build();

        final ChatDto expected = new ChatDto(
                chatId, "Test chat", new ArrayList<>(), null, null);

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.of(chat));

        // when
        final ChatDto actual = chatService.findById(chatId, null, true);

        // then
        final ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(chatRepositoryMock).findById(idCaptor.capture());
        assertEquals(chatId, idCaptor.getValue());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test find chat by id - user is not owner of the chat")
    void testFindChatByIdUserIsNotOwner() {
        // given
        final UUID chatId = UUID.randomUUID();

        final Chat chat = Chat.builder()
                .id(chatId)
                .name("Test chat")
                .messages(new ArrayList<>())
                .database(Database.builder().user(testUser2).build())
                .build();

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.of(chat));
        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationServiceMock).ifNotAdminOrSelfRequestThrowAccessDenied(testUser2.getId());

        // then
        assertThrows(AccessDeniedException.class,
                () -> chatService.findById(chatId, null, true));
    }

    @Test
    @DisplayName("Test find chat by id not found")
    void testFindChatByIdNotFound() {
        // given
        final UUID chatId = UUID.randomUUID();

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> chatService.findById(chatId, null, true));

        // then
        assertEquals("Chat of id: \"" + chatId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Test find chats by database id")
    void testFindChatsByDatabaseId() throws EntityNotFoundException {
        // given
        final UUID databaseId = UUID.randomUUID();

        final Database database = Database.builder()
                .id(databaseId)
                .name("Test database")
                .user(testUser)
                .build();

        final Chat chat1 = Chat.builder()
                .id(UUID.randomUUID())
                .name("Find the oldest user")
                .messages(new ArrayList<>())
                .database(database)
                .build();

        final Chat chat2 = Chat.builder()
                .id(UUID.randomUUID())
                .name("find emails of all users")
                .messages(new ArrayList<>())
                .database(database)
                .build();

        final List<Chat> chats = List.of(chat1, chat2);

        final List<ChatHistoryItem> expected = List.of(
                new ChatHistoryItem(chat1.getId(), "Find the oldest user"),
                new ChatHistoryItem(chat2.getId(), "find emails of all users"));

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.of(database));
        when(chatRepositoryMock.findAllByDatabaseOrderByModificationDateDesc(database)).thenReturn(chats);

        // when
        final List<ChatHistoryItem> actual = chatService.findChatsByDatabaseId(databaseId);

        // then
        final ArgumentCaptor<Database> databaseArgumentCaptor = ArgumentCaptor.forClass(Database.class);
        verify(chatRepositoryMock).findAllByDatabaseOrderByModificationDateDesc(databaseArgumentCaptor.capture());
        assertEquals(database, databaseArgumentCaptor.getValue());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test find chats by database id - user is not database owner")
    void testFindChatsByDatabaseIdUserIsNotOwner() {
        // given
        final UUID databaseId = UUID.randomUUID();

        final Database database = Database.builder()
                .id(databaseId)
                .name("Test database")
                .user(testUser2)
                .build();

        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationServiceMock).ifNotAdminOrSelfRequestThrowAccessDenied(testUser2.getId());
        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.of(database));

        // then
        assertThrows(AccessDeniedException.class, () -> chatService.findChatsByDatabaseId(databaseId));
    }

    @Test
    @DisplayName("Test find chats by database id not found")
    void testFindChatsByDatabaseIdNotFound() {
        // given
        final UUID databaseId = UUID.randomUUID();

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> chatService.findChatsByDatabaseId(databaseId));

        // then
        assertEquals("Database of id: \"" + databaseId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Test create chat")
    void testCreate() throws EntityNotFoundException {
        // given
        final UUID databaseId = UUID.randomUUID();

        final Database database = Database.builder()
                .id(databaseId)
                .name("Test database")
                .user(testUser)
                .build();

        final Chat chat = Chat.builder()
                .id(UUID.randomUUID())
                .name("New chat")
                .messages(new ArrayList<>())
                .database(database)
                .build();

        final ChatDto expected = new ChatDto(
                chat.getId(), "New chat", new ArrayList<>(), null, databaseId);

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.of(database));
        when(chatRepositoryMock.save(any())).thenReturn(chat);

        // when
        final ChatDto actual = chatService.create(databaseId);

        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test create chat - user is not owner of the database")
    void testCreateUserIsNotOwner() {
        // given
        final UUID databaseId = UUID.randomUUID();

        final Database database = Database.builder()
                .id(databaseId)
                .name("Test database")
                .user(testUser2)
                .build();

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.of(database));
        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationServiceMock).ifNotAdminOrSelfRequestThrowAccessDenied(testUser2.getId());

        // then
        assertThrows(AccessDeniedException.class, () -> chatService.create(databaseId));
    }

    @Test
    @DisplayName("Test create chat database not found")
    void testCreateDatabaseNotFound() {
        // given
        final UUID databaseId = UUID.randomUUID();

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> chatService.create(databaseId));

        // then
        assertEquals("Database of id: \"" + databaseId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Test add empty message to chat")
    void testAddEmptyMessageToChat() throws EntityNotFoundException {
        // given
        final UUID chatId = UUID.randomUUID();

        final Chat chat = Chat.builder()
                .id(chatId)
                .name("Visualize age of users")
                .messages(new ArrayList<>())
                .database(Database.builder().user(testUser).build())
                .build();

        final ChatQueryWithResponse expected = ChatQueryWithResponse.builder()
                .id(UUID.randomUUID())
                .chat(chat)
                .build();

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.of(chat));
        when(messageRepositoryMock.save(any())).thenReturn(expected);

        // when
        final ChatQueryWithResponse actual = chatService.addEmptyMessageToChat(chatId);

        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test add empty message to chat - user is not owner of the chat")
    void testAddMessageToChatUserIsNotOwner() {
        // given
        final UUID chatId = UUID.randomUUID();

        final Chat chat = Chat.builder()
                .id(chatId)
                .name("Visualize age of users")
                .messages(new ArrayList<>())
                .database(Database.builder().user(testUser2).build())
                .build();

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.of(chat));
        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationServiceMock).ifNotAdminOrSelfRequestThrowAccessDenied(testUser2.getId());
        // then
        assertThrows(AccessDeniedException.class, () -> chatService.addEmptyMessageToChat(chatId));
    }

    @Test
    @DisplayName("Test add message to empty chat not found")
    void testAddMessageToChatNotFound() {
        // given
        final UUID chatId = UUID.randomUUID();

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> chatService.addEmptyMessageToChat(chatId));

        // then
        assertEquals("Chat of id: \"" + chatId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Test rename chat")
    void testRenameChat() throws EntityNotFoundException {
        // given
        final UUID chatId = UUID.randomUUID();

        final String newName = "User's age distribution";

        final Chat chat = Chat.builder()
                .id(chatId)
                .name("Visualize age of users")
                .messages(new ArrayList<>())
                .database(Database.builder().user(testUser).build())
                .build();

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.of(chat));

        // when
        chatService.renameChat(chatId, newName);

        // then
        final ArgumentCaptor<Chat> chatCaptor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepositoryMock).save(chatCaptor.capture());
        assertEquals(newName, chatCaptor.getValue().getName());
    }

    @Test
    @DisplayName("Test rename chat - user is not owner of the chat")
    void testRenameChatUserIsNotOwner() {
        // given
        final UUID chatId = UUID.randomUUID();

        final String newName = "User's age distribution";

        final Chat chat = Chat.builder()
                .id(chatId)
                .name("Visualize age of users")
                .messages(new ArrayList<>())
                .database(Database.builder().user(testUser2).build())
                .build();

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.of(chat));
        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationServiceMock).ifNotAdminOrSelfRequestThrowAccessDenied(testUser2.getId());

        // then
        assertThrows(AccessDeniedException.class, () -> chatService.renameChat(chatId, newName));
    }

    @Test
    @DisplayName("Test rename chat name longer than limit")
    void testRenameChatLongName() throws EntityNotFoundException {
        // given
        final UUID chatId = UUID.randomUUID();

        final String newName = "Histogram of user's age distribution";

        final Chat chat = Chat.builder()
                .id(chatId)
                .name("Visualize age of users")
                .messages(new ArrayList<>())
                .database(Database.builder().user(testUser).build())
                .build();

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.of(chat));

        // when
        chatService.renameChat(chatId, newName);

        // then
        final ArgumentCaptor<Chat> chatCaptor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepositoryMock).save(chatCaptor.capture());
        assertEquals("Histogram of user's age distribu", chatCaptor.getValue().getName());
    }

    @Test
    @DisplayName("Test rename chat not found")
    void testRenameChatNotFound() {
        // given
        final UUID chatId = UUID.randomUUID();

        final String newName = "User's age distribution";

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> chatService.renameChat(chatId, newName));

        // then
        assertEquals("Chat of id: \"" + chatId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Test delete chat by id")
    void testDeleteChatById() {
        // given
        final UUID chatId = UUID.randomUUID();

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.of(Chat.builder()
                .id(chatId)
                .database(Database.builder().user(testUser).build())
                .build()));

        // when
        chatService.deleteChatById(chatId);

        // then
        final ArgumentCaptor<UUID> repositoryIdCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<String> plotIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatRepositoryMock).deleteById(repositoryIdCaptor.capture());
        verify(plotServiceMock).deletePlots(plotIdCaptor.capture());
        assertEquals(chatId, repositoryIdCaptor.getValue());
        assertEquals(chatId.toString(), plotIdCaptor.getValue());
    }

    @Test
    @DisplayName("Test delete chat by id - user is not owner of the chat")
    void testDeleteChatByIdUserIsNotOwner() {
        // given
        final UUID chatId = UUID.randomUUID();

        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.of(Chat.builder()
                .id(chatId)
                .database(Database.builder().user(testUser2).build())
                .build()));
        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationServiceMock).ifNotAdminOrSelfRequestThrowAccessDenied(testUser2.getId());

        // then
        assertThrows(AccessDeniedException.class, () -> chatService.deleteChatById(chatId));
    }
}