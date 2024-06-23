package com.janbabak.noqlbackend.dao;


import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract public class MySqlTest extends LocalDatabaseTest {

    public static final String COMMAND_SEPARATOR = "-- command separator";
    static final String CONTAINER_NAME = "mysql:8.3.0";

    static {
        try (MySQLContainer<?> mySQLContainer = new MySQLContainer<>(CONTAINER_NAME)) {
            databaseContainer = mySQLContainer
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
}
