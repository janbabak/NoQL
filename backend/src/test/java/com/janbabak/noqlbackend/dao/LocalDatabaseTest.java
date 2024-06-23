package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.database.DatabaseServiceFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for testing classes that access databases.<br />
 * It uses Testcontainers to run databases in Docker container.<br />
 * It doesn't change the Spring profile, so the database is not used by ORM repositories.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class LocalDatabaseTest {
    protected static final String DATABASE_NAME = "test-database";
    static final String DATABASE_USERNAME = "test-user";
    static final String DATABASE_PASSWORD = "test-password";

    @Container
    protected static JdbcDatabaseContainer<?> databaseContainer;

    protected DatabaseDAO databaseDAO;
    protected Database database;

    @BeforeAll
    protected void setUp() throws DatabaseConnectionException, DatabaseExecutionException {
        database = Database.builder()
                .name("Local testing database")
                .host(databaseContainer.getHost())
                .database(databaseContainer.getDatabaseName())
                .userName(databaseContainer.getUsername())
                .password(databaseContainer.getPassword())
                .port(databaseContainer.getFirstMappedPort())
                .chats(new ArrayList<>())
                .engine(getDatabaseEngine())
                .build();

        databaseDAO = DatabaseServiceFactory.getDatabaseDAO(database);

        for (String script : getInitializationScripts()) {
            databaseDAO.updateDatabase(script);
        }
    }

    protected Integer getDatabasePort() {
        return databaseContainer.getFirstMappedPort();
    }

    /**
     * Get list of scripts to create database. Each script is one command in case of MySQL, multiple commands in one
     * script don't work.
     *
     * @return list of commands.
     */
    protected abstract List<String> getInitializationScripts();

    protected abstract DatabaseEngine getDatabaseEngine();
}
