package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
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

    public ResultSet executeQuery(String query) {
        return databaseDAO.query(query);
    }

    /**
     * Find database by id.
     * @param id identifier
     * @return database
     */
    public Database findById(UUID id) {
        return databaseRepository.findById(id).get(); // TODO: handle errors by exceptions
    }

    /**
     * Find all databases.
     * @return list of databases
     */
    public List<Database> findAll() {
        return databaseRepository.findAll();
    }

    /**
     * Create new database object - persist it.
     * @param database object to be saved
     * @return saved object with id
     */
    public Database create(Database database) {
        return databaseRepository.save(database);
    }

    /**
     * Update not null parameters of database.
     * @param id identifier of the database object to update
     * @param data new data
     * @return updated object
     * @throws Exception if object of specified id not exist
     */
    public Database update(UUID id, Database data) throws Exception {
        Optional<Database> optionalDatabase = databaseRepository.findById(id);
        Database database;

        if (optionalDatabase.isPresent()) {
            database = optionalDatabase.get();
        } else {
            throw new Exception("entity not found"); // TODO better exception
        }

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

        return databaseRepository.save(database);
    }

    /**
     * Delete database by id.
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
