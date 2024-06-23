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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Arrays;

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

    static final String POSTGRES_CONTAINER_NAME = "postgres:16-alpine";
    static final String MYSQL_CONTAINER_NAME = "mysql:8.3.0";

    public static final String COMMAND_SEPARATOR = "-- command separator";

    @Container
    protected static PostgreSQLContainer<?> postgresContainer;

    @Container
    protected static MySQLContainer<?> mySqlContainer;

    protected PostgresDAO postgresDAO;
    protected MySqlDAO mySqlDAO;
    protected Database postgresDatabase;
    protected Database mySqlDatabase;

    static {
        try (PostgreSQLContainer<?> container = new PostgreSQLContainer<>(POSTGRES_CONTAINER_NAME)) {
            postgresContainer = container
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(DATABASE_USERNAME)
                    .withPassword(DATABASE_PASSWORD);
        }
        try (MySQLContainer<?> container = new MySQLContainer<>(MYSQL_CONTAINER_NAME)) {
            mySqlContainer = container
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(DATABASE_USERNAME)
                    .withPassword(DATABASE_PASSWORD);
        }
    }

    @BeforeAll
    protected void setUp() throws DatabaseConnectionException, DatabaseExecutionException {
        InitScripts initScripts = getPostgresInitializationScripts();

        postgresDatabase = Database.builder()
                .name("Local postgres testing database")
                .host(postgresContainer.getHost())
                .database(postgresContainer.getDatabaseName())
                .userName(postgresContainer.getUsername())
                .password(postgresContainer.getPassword())
                .port(postgresContainer.getFirstMappedPort())
                .chats(new ArrayList<>())
                .engine(DatabaseEngine.POSTGRES)
                .build();

        postgresDAO = new PostgresDAO(postgresDatabase);

        if (initScripts.postgresScript != null) {
            postgresDAO.updateDatabase(initScripts.postgresScript);
        }

        mySqlDatabase = Database.builder()
                .name("Local MySQL testing database")
                .host(mySqlContainer.getHost())
                .database(mySqlContainer.getDatabaseName())
                .userName(mySqlContainer.getUsername())
                .password(mySqlContainer.getPassword())
                .port(mySqlContainer.getFirstMappedPort())
                .chats(new ArrayList<>())
                .engine(DatabaseEngine.MYSQL)
                .build();

        mySqlDAO = new MySqlDAO(mySqlDatabase);

        if (initScripts.mySqlScript != null) {
            for (String script : Arrays.stream(initScripts.mySqlScript.split(COMMAND_SEPARATOR)).toList()) {
                mySqlDAO.updateDatabase(script);
            }
        }

    }

    protected Integer getPostgresPort() {
        return postgresContainer.getFirstMappedPort();
    }

    protected Integer getMySqlPort() {
        return mySqlContainer.getFirstMappedPort();
    }

    /**
     * Get scripts for initialization of the databases
     */
    protected abstract InitScripts getPostgresInitializationScripts();

    public record InitScripts(String postgresScript, String mySqlScript) {

        public static InitScripts mySql(String mySqlScript) {
            return new InitScripts(null, mySqlScript);
        }

        public static InitScripts postgres(String postgresScript) {
            return new InitScripts(postgresScript, null);
        }
    }
}
