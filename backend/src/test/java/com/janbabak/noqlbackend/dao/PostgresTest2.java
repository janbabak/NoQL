package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.entity.Database;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract public class PostgresTest2 {

    static String DATABASE_NAME = "test-database";
    static String DATABASE_USERNAME = "test-user";
    static String DATABASE_PASSWORD = "test-password";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_USERNAME)
            .withPassword(DATABASE_PASSWORD);

    protected PostgresDAO postgresDAO;

    protected Database postgresDatabase;

    @BeforeAll
    protected void setUp() throws DatabaseConnectionException, DatabaseExecutionException {
        postgresDatabase = Database.builder()
                .name("Local testing postgres")
                .host(postgres.getHost())
                .database(postgres.getDatabaseName())
                .userName(postgres.getUsername())
                .password(postgres.getPassword())
                .port(postgres.getFirstMappedPort())
                .build();

        postgresDAO = new PostgresDAO(postgresDatabase);

        String createScript = getCreateScript();
        if (createScript != null) {
            postgresDAO.updateDatabase(createScript);
        }
    }

    protected Integer getDatabasePort() {
        return postgres.getFirstMappedPort();
    }

    protected abstract String getCreateScript();
}