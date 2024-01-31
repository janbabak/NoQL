package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.database.Database;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BaseDatabaseService {
    protected DatabaseDAO databaseDAO;

    @Autowired
    protected DatabaseRepository databaseRepository;

    public ResultSet executeQuery(String query) throws DatabaseConnectionException, DatabaseExecutionException {
        return databaseDAO.query(query);
    }

    /**
     * Find database by id.
     *
     * @param id identifier
     * @return database
     * @throws EntityNotFoundException database of specified id not found.
     */
    public Database findById(UUID id) throws EntityNotFoundException {
        Optional<Database> optionalDatabase = databaseRepository.findById(id);

        if (optionalDatabase.isEmpty()) {
            throw new EntityNotFoundException(EntityNotFoundException.Entity.DATABASE, id);
        }

        return optionalDatabase.get();
    }

    /**
     * Find all databases.
     *
     * @return list of databases
     */
    public List<Database> findAll() {
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
        DatabaseServiceFactory.getDatabaseDAO(database).testConnection();

        return databaseRepository.save(database);
    }

    /**
     * Update not null parameters of database.
     *
     * @param id   identifier of the database object to update
     * @param data new data
     * @return updated object
     * @throws EntityNotFoundException database of specified id not found.
     * @throws DatabaseConnectionException connection to the updated database failed.
     */
    public Database update(UUID id, Database data) throws EntityNotFoundException, DatabaseConnectionException {
        Optional<Database> optionalDatabase = databaseRepository.findById(id);
        if (optionalDatabase.isEmpty()) {
            throw new EntityNotFoundException(EntityNotFoundException.Entity.DATABASE, id);
        }

        Database database = optionalDatabase.get();

        if (data.getName() != null) {
            database.setName(data.getName());
        }
        if (data.getHost() != null) {
            database.setHost(data.getHost());
        }
        if (data.getPort() != null) {
            database.setPort(data.getPort());
        }
        if (data.getDatabase() != null) {
            database.setDatabase(data.getDatabase());
        }
        if (data.getUserName() != null) {
            database.setUserName(data.getUserName());
        }
        if (data.getPassword() != null) {
            database.setPassword(data.getPassword());
        }
        if (data.getEngine() != null) {
            database.setEngine(data.getEngine());
        }
        if (data.getIsSQL() != null) {
            database.setIsSQL(data.getIsSQL());
        }

        DatabaseServiceFactory.getDatabaseDAO(database).testConnection();

        return databaseRepository.save(database);
    }

    /**
     * Delete database by id.
     *
     * @param id identifier
     */
    public void deleteById(UUID id) {
        databaseRepository.deleteById(id);
    }

    // TODO: delete
    public void insertSampleData() {
        Database database1 = Database.builder()
                .name("Postgres 1")
                .host("localhost")
                .port("5432")
                .database("database")
                .userName("user")
                .password("password")
                .engine(DatabaseEngine.POSTGRES)
                .isSQL(true)
                .build();

        Database database2 = Database.builder()
                .name("Postgres 2")
                .host("janbabak")
                .port("5432")
                .database("data")
                .userName("user")
                .password("password")
                .engine(DatabaseEngine.POSTGRES)
                .isSQL(true)
                .build();

        databaseRepository.save(database1);
        databaseRepository.save(database2);
    }
}
