package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.UserAlreadyExistsException;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
import com.janbabak.noqlbackend.model.database.CreateDatabaseRequest;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.database.UpdateDatabaseRequest;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.user.RegisterRequest;
import com.janbabak.noqlbackend.service.chat.ChatQueryWithResponseService;
import com.janbabak.noqlbackend.service.chat.ChatService;
import com.janbabak.noqlbackend.service.database.DatabaseEntityService;
import com.janbabak.noqlbackend.service.database.DatabaseServiceFactory;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

/**
 * Test services that work with ORM entities.
 */
@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EntityServiceIntegrationTest {

    @Autowired
    private DatabaseEntityService databaseService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatQueryWithResponseService chatQueryWithResponseService;

    @Autowired
    private ChatQueryWithResponseRepository chatQueryWithResponseRepository;

    @Autowired
    private AuthenticationService authenticationService;

    private final MockedStatic<DatabaseServiceFactory> databaseServiceFactoryMock =
            Mockito.mockStatic(DatabaseServiceFactory.class);

    private final DatabaseDAO databaseDaoMock = mock(DatabaseDAO.class);

    private User testUser;

    private User testAdmin;

    @BeforeAll
    void setUp() throws UserAlreadyExistsException {
        RegisterRequest registerUserRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com")
                .password("password")
                .build();

        RegisterRequest registerAdminRequest = RegisterRequest.builder()
                .firstName("admin")
                .lastName("admin")
                .email("admin.admin@gmail.com")
                .password("password")
                .build();

        testUser = authenticationService.register(registerUserRequest, Role.USER).user();
        testAdmin = authenticationService.register(registerAdminRequest, Role.ADMIN).user();

        AuthenticationService.authenticateUser(testUser);
    }

    @AfterEach
    void tearDown() {
        databaseServiceFactoryMock.close(); // deregister the mock in current thread
    }


    @Test
    @DisplayName("Test create, modify, and delete objects")
    void testCreateModifyAndDeleteObjectDatabase() throws DatabaseConnectionException, EntityNotFoundException {
        // create objects
        List<Database> databases = createDatabases();
        Database postgres = databases.get(0);
        Database mysql = databases.get(1);
        Database adminMysql = databases.get(2);

        AuthenticationService.authenticateUser(testUser);
        List<ChatDto> postgresChats = createChats(postgres, 3);
        List<ChatDto> mysqlChats = createChats(mysql, 3);

        AuthenticationService.authenticateUser(testAdmin);
        createChats(adminMysql, 2);

        AuthenticationService.authenticateUser(testUser);
        addMessagesToChat(postgresChats.get(0).getId(), 3);
        addMessagesToChat(postgresChats.get(1).getId(), 1);
        addMessagesToChat(postgresChats.get(2).getId(), 4);
        addMessagesToChat(mysqlChats.get(0).getId(), 4);
        addMessagesToChat(mysqlChats.get(2).getId(), 2);

        // update objects
        renameChat(postgresChats.get(2).getId());
        updateDatabase(postgres.getId());

        // delete objects
        deleteChat(postgresChats.get(0).getId());
        deleteMysql(mysql.getId());
        deletePostgres(postgres.getId());
        deleteAdminMysql(adminMysql.getId());
    }

    /**
     * Create two databases and verify that they were created.
     *
     * @return list of created databases (postgres and mysql)
     * @throws DatabaseConnectionException should not happen
     */
    List<Database> createDatabases() throws DatabaseConnectionException, EntityNotFoundException {
        CreateDatabaseRequest createPostgresRequest = CreateDatabaseRequest.builder()
                .name("Local Postgres")
                .engine(DatabaseEngine.POSTGRES)
                .port(5432)
                .host("localhost")
                .userName("jan")
                .password("jan-password")
                .userId(testUser.getId())
                .build();

        CreateDatabaseRequest createMysqlRequest = CreateDatabaseRequest.builder()
                .name("Local Postgres")
                .engine(DatabaseEngine.MYSQL)
                .port(3306)
                .host("https://janbabak.com")
                .userName("babak")
                .password("secret")
                .userId(testUser.getId())
                .build();

        CreateDatabaseRequest createAdminMysqlRequest = CreateDatabaseRequest.builder()
                .name("Local Postgres")
                .engine(DatabaseEngine.MYSQL)
                .port(3306)
                .host("https://janbabak.com")
                .userName("babak")
                .password("secret")
                .userId(testAdmin.getId())
                .build();

        databaseServiceFactoryMock
                .when(() -> DatabaseServiceFactory.getDatabaseDAO(any()))
                .thenReturn(databaseDaoMock);

        Database createdPostgres = databaseService.create(createPostgresRequest);
        Database createdMsql = databaseService.create(createMysqlRequest);

        AuthenticationService.authenticateUser(testAdmin);
        Database createdAdminMysql = databaseService.create(createAdminMysqlRequest);

        assertEquals(3, databaseService.findAll().size());
        assertEquals(1, databaseService.findAll(testAdmin.getId()).size());

        AuthenticationService.authenticateUser(testUser);

        assertEquals(2, databaseService.findAll(testUser.getId()).size());

        return List.of(createdPostgres, createdMsql, createdAdminMysql);
    }

    /**
     * Create three chats in a database and verify that they were created.
     *
     * @param database database to create chats in
     * @param count   number of chats to create
     * @return list of crated chats
     * @throws EntityNotFoundException should not happen
     */
    List<ChatDto> createChats(Database database, int count) throws EntityNotFoundException {
        List<ChatDto> chats = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            chats.add(chatService.create(database.getId()));
        }

        // verify that the chats were created
        assertEquals(count, chatService.findChatsByDatabaseId(database.getId()).size());

        return chats;
    }

    /**
     * Add messages to chat and verify that they were added.
     *
     * @param chatId           chat identifier
     * @param numberOfMessages number of messages to add
     * @throws EntityNotFoundException should not happen
     */
    void addMessagesToChat(UUID chatId, int numberOfMessages) throws EntityNotFoundException {
        CreateChatQueryWithResponseRequest request = CreateChatQueryWithResponseRequest.builder()
                .nlQuery("Find all users older than 25")
                .llmResult(
                        // language=JSON
                        """
                                {
                                     "databaseQuery": "SELECT * FROM users WHERE age > 25",
                                     "generatePlot": false,
                                     "pythonCode": null
                                 }""")
                .build();

        for (int i = 0; i < numberOfMessages; i++) {
            chatService.addMessageToChat(chatId, request);
        }

        // verify that the messages were added
        assertEquals(numberOfMessages, chatQueryWithResponseService.getMessagesFromChat(chatId).size());
    }

    /**
     * Rename chat and verify that it was renamed.
     *
     * @param chatId chat identifier
     * @throws EntityNotFoundException should not happen
     */
    void renameChat(UUID chatId) throws EntityNotFoundException {
        chatService.renameChat(chatId, "This is the new name of the chat :)");

        assertEquals("This is the new name of the chat", chatService.findById(chatId).getName());
    }

    /**
     * Update database and verify that it was updated.
     *
     * @param databaseId database identifier
     * @throws EntityNotFoundException     should not happen
     * @throws DatabaseConnectionException should not happen
     */
    void updateDatabase(UUID databaseId) throws EntityNotFoundException, DatabaseConnectionException {
        UpdateDatabaseRequest request = UpdateDatabaseRequest.builder()
                .name("New name")
                .host("localhost")
                .password("new-password")
                .port(3333)
                .build();

        databaseService.update(databaseId, request);

        Database updatedDatabase = databaseService.findById(databaseId);

        // verify that the database was updated
        assertEquals(request.getName(), updatedDatabase.getName());
        assertEquals(request.getHost(), updatedDatabase.getHost());
        assertEquals(request.getPassword(), updatedDatabase.getPassword());
        assertEquals(request.getPort(), updatedDatabase.getPort());
    }

    /**
     * Delete chat by id and verify that it was deleted and related messages were deleted and other objects were not.
     *
     * @param chatId chat identifier
     */
    void deleteChat(UUID chatId) throws EntityNotFoundException {
        assertEquals(8, chatRepository.findAll().size());
        assertEquals(14, chatQueryWithResponseRepository.findAll().size());
        assertNotNull(chatService.findById(chatId));

        chatService.deleteChatById(chatId);

        assertThrows(EntityNotFoundException.class, () -> chatService.findById(chatId));
        assertEquals(7, chatRepository.findAll().size());
        assertEquals(11, chatQueryWithResponseRepository.findAll().size());
        assertEquals(2, databaseService.findAll(testUser.getId()).size());
    }

    /**
     * Delete database by id and verify that it was deleted and related chats and messages were deleted
     * and other objects were not.
     *
     * @param databaseId database identifier
     * @throws EntityNotFoundException should not happen
     */
    void deleteMysql(UUID databaseId) throws EntityNotFoundException {
        assertEquals(2, databaseService.findAll(testUser.getId()).size());
        assertEquals(7, chatRepository.findAll().size());
        assertEquals(11, chatQueryWithResponseRepository.findAll().size());
        assertNotNull(databaseService.findById(databaseId));

        databaseService.deleteById(databaseId);

        assertThrows(EntityNotFoundException.class, () -> databaseService.findById(databaseId));
        assertEquals(1, databaseService.findAll(testUser.getId()).size());
        assertEquals(4, chatRepository.findAll().size());
        assertEquals(5, chatQueryWithResponseRepository.findAll().size());
    }

    /**
     * Delete database by id and verify that it was deleted and related chats and messages were deleted.
     *
     * @param databaseId database identifier
     */
    void deletePostgres(UUID databaseId) {
        databaseService.deleteById(databaseId);

        assertEquals(0, databaseService.findAll(testUser.getId()).size());
        assertEquals(2, chatRepository.findAll().size());
        assertEquals(0, chatQueryWithResponseRepository.findAll().size());
    }

    /**
     * Delete database by id and verify that it was deleted and related chats and messages were deleted
     * and other objects were not.
     *
     * @param databaseId database identifier
     */
    void deleteAdminMysql(UUID databaseId) {
        // testUser is not owner of the database
        assertThrows(AccessDeniedException.class, () -> databaseService.deleteById(databaseId));

        AuthenticationService.authenticateUser(testAdmin);
        databaseService.deleteById(databaseId);
    }
}
