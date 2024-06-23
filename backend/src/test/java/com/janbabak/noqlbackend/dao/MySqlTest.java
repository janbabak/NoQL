package com.janbabak.noqlbackend.dao;


import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract class for testing classes that access MySQL database.<br />
 * It uses Testcontainers to run the MySQL database in Docker container.<br />
 * It doesn't change the Spring profile, so the MySQL container is not used by ORM repositories.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract public class MySqlTest extends LocalDatabaseTest {

    static final String CONTAINER_NAME = "mysql:8.3.0";
    public static final String COMMAND_SEPARATOR = "-- command separator";

    @Container
    protected static MySQLContainer<?> databaseContainer;

    static {
        try (MySQLContainer<?> mySQLContainer = new MySQLContainer<>(CONTAINER_NAME)) {
            databaseContainer = mySQLContainer
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(DATABASE_USERNAME)
                    .withPassword(DATABASE_PASSWORD);
        }
    }

    @BeforeAll
    @Override
    protected void setUp() throws DatabaseConnectionException, DatabaseExecutionException {
        super.setUp();
    }

    protected DatabaseEngine getDatabaseEngine() {
        return DatabaseEngine.MYSQL;
    }

    protected JdbcDatabaseContainer<?> getDatabaseContainer() {
        return databaseContainer;
    }
}
