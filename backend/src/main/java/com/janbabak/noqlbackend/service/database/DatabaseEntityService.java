package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.dao.repository.UserRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.database.*;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.DATABASE;
import static com.janbabak.noqlbackend.service.chat.ChatService.NEW_CHAT_NAME;

/**
 * Database Entity Service handles CRUD operations and similar tasks for Database Entities, utilizing the
 * {@link DatabaseRepository} as a DAO.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseEntityService {

    private final DatabaseRepository databaseRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final AuthenticationService authenticationService;
    private final DatabaseCredentialsEncryptionService encryptionService;
    private final DatabaseServiceFactory databaseServiceFactory;

    /**
     * Find database by id.
     *
     * @param databaseId database identifier
     * @return database
     * @throws EntityNotFoundException                                   database of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the database.
     */
    public Database findById(UUID databaseId) throws EntityNotFoundException {
        log.info("Get database by id={}.", databaseId);

        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(database.getUserId());

        return database;
    }

    /**
     * Find all databases (from all users)
     *
     * @return list of databases
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin
     */
    public List<Database> findAll() {
        return findAll(null);
    }

    /**
     * Find all databases with filter.
     *
     * @param userId user identifier - filter by user id. When null - return all databases.
     *               User can see only his databases. Admin can see all.
     * @return list of databases
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the database.
     */
    public List<Database> findAll(UUID userId) {
        log.info("Get all databases. Filter by userId={}.", userId);

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(userId);

        return userId == null
                ? databaseRepository.findAll()
                : databaseRepository.findAllByUserId(userId);
    }

    /**
     * Create new database object - persist it.
     *
     * @param request database data
     * @return saved object with id
     * @throws DatabaseConnectionException                               if connection to the database failed.
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the database.
     */
    public Database create(CreateDatabaseRequest request) throws DatabaseConnectionException, EntityNotFoundException {
        log.info("Create new database.");

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(request.getUserId());

        User user = userRepository.findById(request.getUserId()).orElseThrow(
                () -> new EntityNotFoundException(EntityNotFoundException.Entity.USER, request.getUserId()));

        Database database = Database.builder()
                .name(request.getName())
                .host(request.getHost())
                .port(request.getPort())
                .database(request.getDatabase())
                .userName(request.getUserName())
                .password(encryptionService.encryptCredentials(request.getPassword()))
                .engine(request.getEngine())
                .user(user)
                .build();

        testConnection(database);

        database = databaseRepository.save(database);

        // create default chat
        if (request.getCreateDefaultChat()) {
            Chat chat = Chat.builder()
                    .name(NEW_CHAT_NAME)
                    .modificationDate(Timestamp.from(Instant.now()))
                    .database(database)
                    .build();

            chatRepository.save(chat);
        }

        return database;
    }

    /**
     * Update not null parameters of database.
     *
     * @param databaseId identifier of the database object to update
     * @param data       new data
     * @return updated object
     * @throws EntityNotFoundException                                   database of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the database.
     * @throws DatabaseConnectionException                               connection to the updated database failed.
     */
    public Database update(UUID databaseId, UpdateDatabaseRequest data)
            throws EntityNotFoundException, DatabaseConnectionException {

        log.info("Update database of id={}.", databaseId);

        Database database = findById(databaseId);

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(database.getUserId());

        if (data.getName() != null) database.setName(data.getName());
        if (data.getHost() != null) database.setHost(data.getHost());
        if (data.getPort() != null) database.setPort(data.getPort());
        if (data.getDatabase() != null) database.setDatabase(data.getDatabase());
        if (data.getUserName() != null) database.setUserName(data.getUserName());
        if (data.getPassword() != null) database.setPassword(encryptionService.encryptCredentials(data.getPassword()));
        if (data.getEngine() != null) database.setEngine(data.getEngine());

        testConnection(database);

        return databaseRepository.save(database);
    }

    /**
     * Delete database by id.
     *
     * @param databaseId database identifier
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the database.
     */
    public void deleteById(UUID databaseId) {
        log.info("Delete database by id={}.", databaseId);

        Optional<Database> database = databaseRepository.findById(databaseId);

        if (database.isPresent()) {
            authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(database.get().getUserId());
            databaseRepository.deleteById(databaseId);
        }
    }

    /**
     * Get database structure by database id
     *
     * @param databaseId database identifier
     * @return database structure
     * @throws EntityNotFoundException                                   database of specific id not found
     * @throws DatabaseConnectionException                               connection to the database failed
     * @throws DatabaseExecutionException                                syntax error, ...
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the database.
     */
    public DatabaseStructureDto getDatabaseStructureByDatabaseId(UUID databaseId)
            throws EntityNotFoundException, DatabaseConnectionException, DatabaseExecutionException {

        Database database = findById(databaseId);

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(database.getUserId());

        return databaseServiceFactory.getDatabaseService(database).retrieveSchema().toDto();
    }

    /**
     * Get generated database create script by database id.
     *
     * @param databaseId database identifier
     * @return create script
     * @throws EntityNotFoundException                                   database of specific id not found
     * @throws DatabaseConnectionException                               cannot establish connection with the database
     * @throws DatabaseExecutionException                                syntax error, ...
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the database.
     */
    public String getDatabaseCreateScriptByDatabaseId(UUID databaseId)
            throws EntityNotFoundException, DatabaseConnectionException, DatabaseExecutionException {

        Database database = findById(databaseId);

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(database.getUserId());

        return databaseServiceFactory.getDatabaseService(database).retrieveSchema().generateCreateScript();
    }

    /**
     * Test connection to the database.
     *
     * @param database database to test
     * @throws DatabaseConnectionException cannot establish connection with the database
     */
    private void testConnection(Database database) throws DatabaseConnectionException {
        DatabaseDAO databaseDAO = databaseServiceFactory.getDatabaseDAO(database);
        databaseDAO.testConnection();
    }
}
