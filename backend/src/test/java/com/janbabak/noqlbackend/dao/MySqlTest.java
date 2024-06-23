package com.janbabak.noqlbackend.dao;


import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Database;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract public class MySqlTest {

    public static final String COMMAND_SEPARATOR = "-- command separator";
    protected static final String DATABASE_NAME = "mysql-test-database";
    static final String DATABASE_USERNAME = "test-user";
    static final String DATABASE_PASSWORD = "test-password";
    static final String CONTAINER_NAME = "mysql:8.3.0";

    @Container
    @SuppressWarnings("resource")
    private static final MySQLContainer<?> mysql = new MySQLContainer<>(CONTAINER_NAME)
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_USERNAME)
            .withPassword(DATABASE_PASSWORD);

    protected MySqlDAO mysqlDAO;
    protected Database mysqlDatabase;

    @BeforeAll
    protected void setUp() throws DatabaseConnectionException, DatabaseExecutionException {
        mysqlDatabase = Database.builder()
                .name("Local testing postgres")
                .host(mysql.getHost())
                .database(mysql.getDatabaseName())
                .userName(mysql.getUsername())
                .password(mysql.getPassword())
                .port(mysql.getFirstMappedPort())
                .chats(new ArrayList<>())
                .engine(DatabaseEngine.POSTGRES)
                .build();

        mysqlDAO = new MySqlDAO(mysqlDatabase);

        List<String> scripts = getCreateScript();
        for (String script : scripts) {
            if (script != null) {
                mysqlDAO.updateDatabase(script);
            }
        }
    }

    protected Integer getDatabasePort() {
        return mysql.getFirstMappedPort(); // TODO: ancestor
    }

    /**
     * Get list of scripts to create database. Each script is one command in case of MySQL, multiple commands in one
     * script don't work.
     *
     * @return list of commands.
     */
    protected abstract List<String> getCreateScript(); // TODO: move to ancestor

}
