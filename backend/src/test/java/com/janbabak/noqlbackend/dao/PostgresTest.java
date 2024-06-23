package com.janbabak.noqlbackend.dao;


import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract class for testing classes that access Postgres database.<br />
 * It uses Testcontainers to run the Postgres database in Docker container.<br />
 * It doesn't change the Spring profile, so the Postgres container is not used by ORM repositories.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract public class PostgresTest extends LocalDatabaseTest {
    static final String CONTAINER_NAME = "postgres:16-alpine";

    static {
        try (PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(CONTAINER_NAME)) {
            databaseContainer = postgresContainer
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(DATABASE_USERNAME)
                    .withPassword(DATABASE_PASSWORD);
        }
    }

    @Override
    @BeforeAll
    protected void setUp() throws DatabaseConnectionException, DatabaseExecutionException {
        super.setUp();
    }

    protected DatabaseEngine getDatabaseEngine() {
        return DatabaseEngine.POSTGRES;
    }
}