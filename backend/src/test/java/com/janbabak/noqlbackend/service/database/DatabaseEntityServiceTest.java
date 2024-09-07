package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.PostgresDAO;
import com.janbabak.noqlbackend.dao.ResultSetWrapper;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.dao.repository.UserRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.database.*;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.service.AuthenticationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;

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
    private DatabaseRepository databaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    @SuppressWarnings("unused") // used in the databaseEntityService
    private AuthenticationService authenticationService;

    private final MockedStatic<DatabaseServiceFactory> databaseServiceFactoryMock =
            Mockito.mockStatic(DatabaseServiceFactory.class);

    private final User testUser = User.builder()
            .id(UUID.randomUUID())
            .build();

    @AfterEach
    void tearDown() {
        databaseServiceFactoryMock.close(); // deregister the mock in the current thread
    }

    private final DatabaseDAO databaseDaoMock = new DatabaseDAO() {

        @Override
        public ResultSetWrapper getSchemasTablesColumns() {
            return null;
        }

        @Override
        public ResultSetWrapper getForeignKeys() {
            return null;
        }

        @Override
        public void testConnection() {
        } // do nothing

        @Override
        protected String createConnectionUrl() {
            return "";
        }
    };

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
        UUID databaseId = UUID.randomUUID();

        Database database = Database.builder()
                .id(databaseId)
                .name("local postgres")
                .user(testUser)
                .build();

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(database));
        Database actual = databaseEntityService.findById(databaseId);

        // then
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(databaseRepository).findById(idCaptor.capture());
        assertEquals(databaseId, idCaptor.getValue());
        assertEquals(database, actual);
    }

    @Test
    @DisplayName("Test find database by id not found")
    void testFindDatabaseByIdNotFound() {
        // given
        UUID databaseId = UUID.randomUUID();

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> databaseEntityService.findById(databaseId));
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(databaseRepository).findById(idCaptor.capture());
        assertEquals(databaseId, idCaptor.getValue());
    }

    @Test
    @DisplayName("Test find all databases")
    void testFindAllDatabases() throws EntityNotFoundException {
        // given
        Database database1 = Database.builder()
                .id(UUID.randomUUID())
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .user(testUser)
                .build();

        Database database2 = Database.builder()
                .id(UUID.randomUUID())
                .engine(DatabaseEngine.MYSQL)
                .name("remote mysql")
                .user(testUser)
                .build();

        Database database3 = Database.builder()
                .id(UUID.randomUUID())
                .engine(DatabaseEngine.MYSQL)
                .name("remote mysql")
                .user(User.builder().id(UUID.randomUUID()).firstName("Different user").build())
                .build();

        List<Database> databases = List.of(database1, database2, database3);
        List<Database> databasesOfTestUser = List.of(database1, database2);

        // when
        when(databaseRepository.findAll()).thenReturn(databases);
        when(databaseRepository.filterByUserId(eq(testUser.getId()))).thenReturn(databasesOfTestUser);
        List<Database> actualAll = databaseEntityService.findAll();
        List<Database> actualTestUserDatabases = databaseEntityService.findAll(testUser.getId());

        // then
        assertEquals(3, actualAll.size());
        assertEquals(databases, actualAll);
        assertEquals(2, actualTestUserDatabases.size());
        assertEquals(databasesOfTestUser, actualTestUserDatabases);
    }

    @Test
    @DisplayName("Test create database")
    void testCreateDatabase() throws DatabaseConnectionException, EntityNotFoundException {
        // given
        CreateDatabaseRequest request = CreateDatabaseRequest.builder()
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .userId(testUser.getId())
                .build();
        Database database = Database.builder()
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .user(testUser)
                .build();

        // when
        when(databaseRepository.save(database)).thenReturn(database);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        databaseServiceFactoryMock
                .when(() -> DatabaseServiceFactory.getDatabaseDAO(database))
                .thenReturn(databaseDaoMock);

        Database actual = databaseEntityService.create(request);

        // then
        ArgumentCaptor<Database> databaseCaptor = ArgumentCaptor.forClass(Database.class);
        verify(databaseRepository).save(databaseCaptor.capture());
        assertEquals(database, databaseCaptor.getValue());
        assertEquals(database, actual);
    }

    @Test
    @DisplayName("Test create database connection failed")
    void testCreateDatabaseConnectionFailed() throws DatabaseConnectionException {
        // given
        CreateDatabaseRequest request = CreateDatabaseRequest.builder()
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .userId(testUser.getId())
                .build();
        Database database = Database.builder()
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .user(testUser)
                .build();

        // when
        PostgresDAO postgresDao = Mockito.mock(PostgresDAO.class);
        doThrow(DatabaseConnectionException.class).when(postgresDao).testConnection();
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        databaseServiceFactoryMock
                .when(() -> DatabaseServiceFactory.getDatabaseDAO(database))
                .thenReturn(postgresDao);

        // then
        assertThrows(DatabaseConnectionException.class, () -> databaseEntityService.create(request));
    }

    @Test
    @DisplayName("Test update database")
    void testUpdateDatabase() throws EntityNotFoundException, DatabaseConnectionException {
        // given
        UUID databaseId = UUID.randomUUID();

        Database database = Database.builder()
                .id(databaseId)
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .port(5432)
                .user(testUser)
                .build();

        UpdateDatabaseRequest updateDatabaseRequest = UpdateDatabaseRequest.builder()
                .name("remote postgres")
                .engine(DatabaseEngine.MYSQL)
                .build();

        Database updatedDatabase = Database.builder()
                .id(databaseId)
                .engine(DatabaseEngine.MYSQL)
                .name("remote postgres")
                .port(5432)
                .user(testUser)
                .build();

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(database));
        when(databaseRepository.save(database)).thenReturn(updatedDatabase);

        databaseServiceFactoryMock
                .when(() -> DatabaseServiceFactory.getDatabaseDAO(database))
                .thenReturn(databaseDaoMock);

        Database actual = databaseEntityService.update(databaseId, updateDatabaseRequest);

        // then
        ArgumentCaptor<Database> databaseCaptor = ArgumentCaptor.forClass(Database.class);
        verify(databaseRepository).save(databaseCaptor.capture());
        assertEquals(updatedDatabase, databaseCaptor.getValue());
        assertEquals(database, actual);
    }

    @Test
    @DisplayName("Test update database not found")
    void testUpdateDatabaseNotFound() {
        // given
        UUID databaseId = UUID.randomUUID();

        UpdateDatabaseRequest updateDatabaseRequest = UpdateDatabaseRequest.builder()
                .name("remote postgres")
                .engine(DatabaseEngine.MYSQL)
                .build();

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class,
                () -> databaseEntityService.update(databaseId, updateDatabaseRequest));
    }

    @Test
    @DisplayName("Test update database connection failed")
    void testUpdateDatabaseConnectionFailed() throws DatabaseConnectionException {
        // given
        UUID databaseId = UUID.randomUUID();

        Database database = Database.builder()
                .id(databaseId)
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .port(5432)
                .user(testUser)
                .build();

        UpdateDatabaseRequest updateDatabaseRequest = UpdateDatabaseRequest.builder()
                .name("remote postgres")
                .password("wrong password")
                .engine(DatabaseEngine.MYSQL)
                .build();

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(database));
        PostgresDAO postgresDao = Mockito.mock(PostgresDAO.class);
        doThrow(DatabaseConnectionException.class).when(postgresDao).testConnection();
        databaseServiceFactoryMock
                .when(() -> DatabaseServiceFactory.getDatabaseDAO(database))
                .thenReturn(postgresDao);

        // then
        assertThrows(DatabaseConnectionException.class,
                () -> databaseEntityService.update(databaseId, updateDatabaseRequest));
    }

    @Test
    @DisplayName("Test delete database by id")
    void testDeleteDatabaseById() {
        // given
        UUID databaseId = UUID.randomUUID();

        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(Database.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .build()));

        // when
        databaseEntityService.deleteById(databaseId);

        // then
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(databaseRepository).deleteById(idCaptor.capture());
        assertEquals(databaseId, idCaptor.getValue());
    }

    @Test
    @DisplayName("Test get database structure")
    void testGetDatabaseStructure()
            throws DatabaseConnectionException, DatabaseExecutionException, EntityNotFoundException {

        // given
        UUID databaseId = UUID.randomUUID();

        Database database = Database.builder()
                .id(databaseId)
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .user(testUser)
                .build();

        PostgresService postgresServiceMock = Mockito.mock(PostgresService.class);

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(database));
        when(DatabaseServiceFactory.getDatabaseDAO(database)).thenReturn(databaseDaoMock);
        when(DatabaseServiceFactory.getDatabaseService(database)).thenReturn(postgresServiceMock);
        when(postgresServiceMock.retrieveSchema()).thenReturn(databaseStructure);

        DatabaseStructureDto actual = databaseEntityService.getDatabaseStructureByDatabaseId(databaseId);

        // then
        assertEquals(databaseStructure.toDto(), actual);
    }

    @Test
    @DisplayName("Test get database structure not found")
    void testGetDatabaseStructureNotFound() {
        // given
        UUID databaseId = UUID.randomUUID();

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class,
                () -> databaseEntityService.getDatabaseStructureByDatabaseId(databaseId));
    }

    @Test
    @DisplayName("Test get database create script")
    void testGetDatabaseCreateScript()
            throws DatabaseConnectionException, DatabaseExecutionException, EntityNotFoundException {

        // given
        UUID databaseId = UUID.randomUUID();

        Database database = Database.builder()
                .id(databaseId)
                .engine(DatabaseEngine.POSTGRES)
                .name("local postgres")
                .user(testUser)
                .build();

        // language=SQL
        String expectedCreateScript = """
                CREATE SCHEMA IF NOT EXISTS public;

                CREATE TABLE IF NOT EXISTS public.user
                (
                    id integer,
                    name character varying,
                    surname character varying,
                    age integer
                );""";

        PostgresService postgresServiceMock = Mockito.mock(PostgresService.class);
        SqlDatabaseStructure sqlDatabaseStructureMock = Mockito.mock(SqlDatabaseStructure.class);

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(database));
        when(DatabaseServiceFactory.getDatabaseDAO(database)).thenReturn(databaseDaoMock);
        when(DatabaseServiceFactory.getDatabaseService(database)).thenReturn(postgresServiceMock);
        when(postgresServiceMock.retrieveSchema()).thenReturn(sqlDatabaseStructureMock);
        when(sqlDatabaseStructureMock.generateCreateScript()).thenReturn(expectedCreateScript);

        String actual = databaseEntityService.getDatabaseCreateScriptByDatabaseId(databaseId);

        // then
        assertEquals(expectedCreateScript, actual);
    }

    @Test
    @DisplayName("Test get database create script not found")
    void testGetDatabaseCreateScriptNotFound() {
        // given
        UUID databaseId = UUID.randomUUID();

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class,
                () -> databaseEntityService.getDatabaseCreateScriptByDatabaseId(databaseId));
    }
}