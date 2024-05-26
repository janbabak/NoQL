package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.database.*;
import com.janbabak.noqlbackend.model.entity.Database;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.DATABASE;

/**
 * Database Entity Service handles CRUD operations and similar tasks for Database Entities, utilizing the
 * {@link DatabaseRepository} as a DAO.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseEntityService {

    private final DatabaseRepository databaseRepository;

    /**
     * Find database by id.
     *
     * @param databaseId database identifier
     * @return database
     * @throws EntityNotFoundException database of specified id not found.
     */
    public Database findById(UUID databaseId) throws EntityNotFoundException {
        log.info("Get database by id={}.", databaseId);

        return databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));
    }

    /**
     * Find all databases.
     *
     * @return list of databases
     */
    public List<Database> findAll() {
        log.info("Get all databases.");

        return databaseRepository.findAll();
    }

    /**
     * Create new database object - persist it.
     *
     * @param database object to be saved
     * @return saved object with id
     * @throws DatabaseConnectionException if connection to the database failed.
     */
    public Database create(Database database) throws DatabaseConnectionException {
        log.info("Create new database.");

        DatabaseServiceFactory.getDatabaseDAO(database).testConnection();

        return databaseRepository.save(database);
    }

    /**
     * Update not null parameters of database.
     *
     * @param databaseId identifier of the database object to update
     * @param data new data
     * @return updated object
     * @throws EntityNotFoundException     database of specified id not found.
     * @throws DatabaseConnectionException connection to the updated database failed.
     */
    public Database update(UUID databaseId, UpdateDatabaseRequest data)
            throws EntityNotFoundException, DatabaseConnectionException {

        log.info("Update database of id={}.", databaseId);

        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        if (data.getName() != null) database.setName(data.getName());
        if (data.getHost() != null) database.setHost(data.getHost());
        if (data.getPort() != null) database.setPort(data.getPort());
        if (data.getDatabase() != null) database.setDatabase(data.getDatabase());
        if (data.getUserName() != null) database.setUserName(data.getUserName());
        if (data.getPassword() != null) database.setPassword(data.getPassword());
        if (data.getEngine() != null) database.setEngine(data.getEngine());

        DatabaseServiceFactory.getDatabaseDAO(database).testConnection();

        return databaseRepository.save(database);
    }

    /**
     * Delete database by id.
     *
     * @param databaseId database identifier
     */
    public void deleteById(UUID databaseId) {
        log.info("Delete database by id={}.", databaseId);

        databaseRepository.deleteById(databaseId);
    }

    /**
     * Get database structure by database id
     *
     * @param databaseId database identifier
     * @return database structure
     * @throws EntityNotFoundException     database of specific id not found
     * @throws DatabaseConnectionException connection to the database failed
     * @throws DatabaseExecutionException  syntax error, ...
     */
    public DatabaseStructureDto getDatabaseStructureByDatabaseId(UUID databaseId)
            throws EntityNotFoundException, DatabaseConnectionException, DatabaseExecutionException {

        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        return DatabaseServiceFactory.getDatabaseService(database).retrieveSchema().toDto();
    }

    /**
     * Get generated database create script by database id.
     * @param databaseId database identifier
     * @return create script
     * @throws EntityNotFoundException database of specific id not found
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException syntax error, ...
     */
    public String getDatabaseCreateScriptByDatabaseId(UUID databaseId)
            throws EntityNotFoundException, DatabaseConnectionException, DatabaseExecutionException {

        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        return DatabaseServiceFactory.getDatabaseService(database).retrieveSchema().generateCreateScript();
    }
}
