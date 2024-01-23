package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.model.database.Database;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.List;
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
