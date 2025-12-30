package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.PostgresDAO;
import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.dao.repository.UserRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.database.*;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseEntityServiceTest {

    @InjectMocks
    private DatabaseEntityService databaseEntityService;

    @Mock
    private DatabaseRepository databaseRepositoryMock;

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    @SuppressWarnings("unused") // used in the databaseEntityService
    private ChatRepository chatRepositoryMock;

    @Mock
    @SuppressWarnings("unused") // used in the databaseEntityService
    private AuthenticationService authenticationServiceMock;

    @Mock
    @SuppressWarnings("unused") // used in the databaseEntityService
    private DatabaseCredentialsEncryptionService encryptionServiceMock;

    @Mock
    private DatabaseDAO databaseDaoMock;

    private final DatabaseServiceFactory databaseServiceFactoryMock = mock(DatabaseServiceFactory.class);

    private static final User testUser = User.builder()
            .id(UUID.randomUUID())
            .build();

    private final SqlDatabaseStructure databaseStructure = new SqlDatabaseStructure(Map.of(
            "public", new SqlDatabaseStructure.Schema("public", Map.of(
                    "user", new SqlDatabaseStructure.Table("user", Map.of(
                            "id", new SqlDatabaseStructure.Column(
                                    "id",
                                    "integer",
                                    false),
                            "name", new SqlDatabaseStructure.Column(
                                    "name",
                                    "character varying",
                                    false),
                            "surname", new SqlDatabaseStructure.Column(
                                    "surname",
                                    "character varying",
                                    false),
                            "age", new SqlDatabaseStructure.Column(
                                    "age",
                                    "integer",
                                    false)
                    )),
                    "address", new SqlDatabaseStructure.Table(
                            "address", Map.of(
                            "user_id", new SqlDatabaseStructure.Column(
                                    "user_id",
                                    "integer",
                                    false,
                                    new SqlDatabaseStructure.ForeignKey(
                                            "public",
                                            "\"user\"",
                                            "id")),
                            "city", new SqlDatabaseStructure.Column(
                                    "city",
                                    "character varying",
                                    false),
                            " id", new SqlDatabaseStructure.Column(
                                    "id",
                                    "integer",
                                    true
                            )
                    ))
            )))
    );

    @Test
    @DisplayName("Test find database by id")
    void testFindDatabaseBy() throws EntityNotFoundException {
        // given
        final UUID databaseId = UUID.randomUUID();

        final Database database = Database.builder()
                .id(databaseId)
                .name("local postgres")
                .user(testUser)
                .build();

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.of(database));

        // when
        final Database actual = databaseEntityService.findById(databaseId);

        // then
        final ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(databaseRepositoryMock).findById(idCaptor.capture());
        assertEquals(databaseId, idCaptor.getValue());
        assertEquals(database, actual);
    }

    @Test
    @DisplayName("Test find database by id not found")
    void testFindDatabaseByIdNotFound() {
        // given
        final UUID databaseId = UUID.randomUUID();

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> databaseEntityService.findById(databaseId));

        // then
        assertEquals("Database of id: \"" + databaseId + "\" not found.", exception.getMessage());
        final ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(databaseRepositoryMock).findById(idCaptor.capture());
        assertEquals(databaseId, idCaptor.getValue());
    }

    @Test
    @DisplayName("Test find all databases")
    void testFindAllDatabases() {
        // given
        final Database database1 = Database.builder()
                .id(UUID.randomUUID())
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .user(testUser)
                .build();

        final Database database2 = Database.builder()
                .id(UUID.randomUUID())
                .engine(DatabaseEngine.MYSQL)
                .name("remote mysql")
                .user(testUser)
                .build();

        final Database database3 = Database.builder()
                .id(UUID.randomUUID())
                .engine(DatabaseEngine.MYSQL)
                .name("remote mysql")
                .user(User.builder().id(UUID.randomUUID()).firstName("Different user").build())
                .build();

        final List<Database> databases = List.of(database1, database2, database3);
        final List<Database> databasesOfTestUser = List.of(database1, database2);

        when(databaseRepositoryMock.findAll()).thenReturn(databases);
        when(databaseRepositoryMock.findAllByUserId(eq(testUser.getId()))).thenReturn(databasesOfTestUser);

        // when
        final List<Database> actualAll = databaseEntityService.findAll();
        final List<Database> actualTestUserDatabases = databaseEntityService.findAll(testUser.getId());

        // then
        assertEquals(3, actualAll.size());
        assertEquals(databases, actualAll);
        assertEquals(2, actualTestUserDatabases.size());
        assertEquals(databasesOfTestUser, actualTestUserDatabases);
    }

    @ParameterizedTest
    @DisplayName("Test create database")
    @MethodSource("createDatabaseRequestDataProvider")
    void testCreateDatabase(CreateDatabaseRequest request, Database database) throws DatabaseConnectionException, EntityNotFoundException {
        // given
        when(databaseRepositoryMock.save(database)).thenReturn(database);
        when(userRepositoryMock.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        when(databaseServiceFactoryMock.getDatabaseDAO(database)).thenReturn(databaseDaoMock);

        // when
        final Database actual = databaseEntityService.create(request);

        // then
        final ArgumentCaptor<Database> databaseCaptor = ArgumentCaptor.forClass(Database.class);
        verify(databaseRepositoryMock).save(databaseCaptor.capture());
        assertEquals(database, databaseCaptor.getValue());
        assertEquals(database, actual);
    }

    static Object[][] createDatabaseRequestDataProvider() {
        return new Object[][]{
                // create database without default chat
                {
                        CreateDatabaseRequest.builder()
                                .engine(DatabaseEngine.POSTGRES)
                                .name("local postgres")
                                .userId(testUser.getId())
                                .createDefaultChat(false)
                                .build(),
                        Database.builder()
                                .engine(DatabaseEngine.POSTGRES)
                                .name("local postgres")
                                .user(testUser)
                                .build()
                },
                // create database with default chat
                {
                        CreateDatabaseRequest.builder()
                                .engine(DatabaseEngine.POSTGRES)
                                .name("local postgres")
                                .userId(testUser.getId())
                                .createDefaultChat(true)
                                .build(),
                        Database.builder()
                                .engine(DatabaseEngine.POSTGRES)
                                .name("local postgres")
                                .user(testUser)
                                .build()
                }
        };
    }

    @Test
    @DisplayName("Test create database connection failed")
    void testCreateDatabaseConnectionFailed() throws DatabaseConnectionException {
        // given
        final CreateDatabaseRequest request = CreateDatabaseRequest.builder()
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .userId(testUser.getId())
                .build();
        final Database database = Database.builder()
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .user(testUser)
                .build();

        final PostgresDAO postgresDao = mock(PostgresDAO.class);
        doThrow(DatabaseConnectionException.class).when(postgresDao).testConnection();
        when(userRepositoryMock.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        when(databaseServiceFactoryMock.getDatabaseDAO(database)).thenReturn(postgresDao);

        // then
        assertThrows(DatabaseConnectionException.class, () -> databaseEntityService.create(request));
    }

    @Test
    @DisplayName("Test update database")
    void testUpdateDatabase() throws EntityNotFoundException, DatabaseConnectionException {
        // given
        final UUID databaseId = UUID.randomUUID();

        final Database database = Database.builder()
                .id(databaseId)
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .port(5432)
                .user(testUser)
                .build();

        final UpdateDatabaseRequest updateDatabaseRequest = UpdateDatabaseRequest.builder()
                .name("remote postgres")
                .engine(DatabaseEngine.MYSQL)
                .build();

        final Database updatedDatabase = Database.builder()
                .id(databaseId)
                .engine(DatabaseEngine.MYSQL)
                .name("remote postgres")
                .port(5432)
                .user(testUser)
                .build();

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.of(database));
        when(databaseRepositoryMock.save(updatedDatabase)).thenReturn(updatedDatabase);
        when(databaseServiceFactoryMock.getDatabaseDAO(database)).thenReturn(databaseDaoMock);

        // when
        final Database actual = databaseEntityService.update(databaseId, updateDatabaseRequest);

        // then
        final ArgumentCaptor<Database> databaseCaptor = ArgumentCaptor.forClass(Database.class);
        verify(databaseRepositoryMock).save(databaseCaptor.capture());
        assertEquals(updatedDatabase, databaseCaptor.getValue());
        assertEquals(database, actual);
    }

    @Test
    @DisplayName("Test update database not found")
    void testUpdateDatabaseNotFound() {
        // given
        final UUID databaseId = UUID.randomUUID();

        final UpdateDatabaseRequest updateDatabaseRequest = UpdateDatabaseRequest.builder()
                .name("remote postgres")
                .engine(DatabaseEngine.MYSQL)
                .build();

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> databaseEntityService.update(databaseId, updateDatabaseRequest));

        // then
        assertEquals("Database of id: \"" + databaseId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Test update database connection failed")
    void testUpdateDatabaseConnectionFailed() throws DatabaseConnectionException {
        // given
        final UUID databaseId = UUID.randomUUID();

        final Database database = Database.builder()
                .id(databaseId)
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .port(5432)
                .user(testUser)
                .build();

        final UpdateDatabaseRequest updateDatabaseRequest = UpdateDatabaseRequest.builder()
                .name("remote postgres")
                .password("wrong password")
                .engine(DatabaseEngine.MYSQL)
                .build();

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.of(database));
        final PostgresDAO postgresDao = mock(PostgresDAO.class);
        doThrow(DatabaseConnectionException.class).when(postgresDao).testConnection();
        when(databaseServiceFactoryMock.getDatabaseDAO(database)).thenReturn(postgresDao);

        // then
        assertThrows(DatabaseConnectionException.class,
                () -> databaseEntityService.update(databaseId, updateDatabaseRequest));
    }

    @Test
    @DisplayName("Test delete database by id")
    void testDeleteDatabaseById() {
        // given
        final UUID databaseId = UUID.randomUUID();

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.of(Database.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .build()));

        // when
        databaseEntityService.deleteById(databaseId);

        // then
        final ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(databaseRepositoryMock).deleteById(idCaptor.capture());
        assertEquals(databaseId, idCaptor.getValue());
    }

    @Test
    @DisplayName("Test get database structure")
    void testGetDatabaseStructure()
            throws DatabaseConnectionException, DatabaseExecutionException, EntityNotFoundException {
        // given
        final UUID databaseId = UUID.randomUUID();

        final Database database = Database.builder()
                .id(databaseId)
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .user(testUser)
                .build();

        final PostgresService postgresServiceMock = mock(PostgresService.class);

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.of(database));
        when(databaseServiceFactoryMock.getDatabaseDAO(database)).thenReturn(databaseDaoMock);
        when(databaseServiceFactoryMock.getDatabaseService(database)).thenReturn(postgresServiceMock);
        when(postgresServiceMock.retrieveSchema()).thenReturn(databaseStructure);

        // when
        final DatabaseStructureDto actual = databaseEntityService.getDatabaseStructureByDatabaseId(databaseId);

        // then
        assertEquals(databaseStructure.toDto(), actual);
    }

    @Test
    @DisplayName("Test get database structure not found")
    void testGetDatabaseStructureNotFound() {
        // given
        final UUID databaseId = UUID.randomUUID();

        // when
        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> databaseEntityService.getDatabaseStructureByDatabaseId(databaseId));

        // then
        assertEquals("Database of id: \"" + databaseId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Test get database create script")
    void testGetDatabaseCreateScript()
            throws DatabaseConnectionException, DatabaseExecutionException, EntityNotFoundException {

        // given
        final UUID databaseId = UUID.randomUUID();

        final Database database = Database.builder()
                .id(databaseId)
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .user(testUser)
                .build();

        // language=SQL
        final String expectedCreateScript = """
                CREATE SCHEMA IF NOT EXISTS public;

                CREATE TABLE IF NOT EXISTS public.user
                (
                    id integer,
                    name character varying,
                    surname character varying,
                    age integer
                );""";

        final PostgresService postgresServiceMock = mock(PostgresService.class);
        final SqlDatabaseStructure sqlDatabaseStructureMock = mock(SqlDatabaseStructure.class);

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.of(database));
        when(databaseServiceFactoryMock.getDatabaseDAO(database)).thenReturn(databaseDaoMock);
        when(databaseServiceFactoryMock.getDatabaseService(database)).thenReturn(postgresServiceMock);
        when(postgresServiceMock.retrieveSchema()).thenReturn(sqlDatabaseStructureMock);
        when(sqlDatabaseStructureMock.generateCreateScript()).thenReturn(expectedCreateScript);

        // when
        final String actual = databaseEntityService.getDatabaseCreateScriptByDatabaseId(databaseId);

        // then
        assertEquals(expectedCreateScript, actual);
    }

    @Test
    @DisplayName("Test get database create script not found")
    void testGetDatabaseCreateScriptNotFound() {
        // given
        final UUID databaseId = UUID.randomUUID();

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> databaseEntityService.getDatabaseCreateScriptByDatabaseId(databaseId));

        // then
        assertEquals("Database of id: \"" + databaseId + "\" not found.", exception.getMessage());
    }
}