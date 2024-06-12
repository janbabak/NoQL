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

/**
 * Abstract class for testing Postgres database. It uses Testcontainers to run Postgres in Docker container.
 * It doesn't change the Spring profile, so the Postgres container is not used by ORM repositories.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract public class PostgresTest {

    static final String DATABASE_NAME = "test-database";
    static final String DATABASE_USERNAME = "test-user";
    static final String DATABASE_PASSWORD = "test-password";
    static final String CONTAINER_VERSION = "16-alpine";

    @Container
    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(CONTAINER_VERSION)
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_USERNAME)
            .withPassword(DATABASE_PASSWORD);

    protected PostgresDAO postgresDAO;
    protected Database postgresDatabase;

    /**
     * Initialize Postgres database and fill it with data.
     *
     * @throws DatabaseConnectionException cannot establish connection to the database
     * @throws DatabaseExecutionException  syntax error in the script
     */
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