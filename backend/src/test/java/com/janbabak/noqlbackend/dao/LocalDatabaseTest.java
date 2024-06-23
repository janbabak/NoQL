package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Database;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.JdbcDatabaseContainer;
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
    private static final String DATABASE_USERNAME = "test-user";
    private static final String DATABASE_PASSWORD = "test-password";

    private static final String POSTGRES_CONTAINER_NAME = "postgres:16-alpine";
    private static final String MYSQL_CONTAINER_NAME = "mysql:8.3.0";

    private static final String COMMAND_SEPARATOR = "-- command separator";

    protected PostgresDAO postgresDAO;
    protected MySqlDAO mySqlDAO;
    protected Database postgresDatabase;
    protected Database mySqlDatabase;

    @Container
    private static PostgreSQLContainer<?> postgresContainer;

    @Container
    private static MySQLContainer<?> mySqlContainer;

    static {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_CONTAINER_NAME);
             MySQLContainer<?> mysql = new MySQLContainer<>(MYSQL_CONTAINER_NAME)) {

            postgresContainer = postgres
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(DATABASE_USERNAME)
                    .withPassword(DATABASE_PASSWORD);

            mySqlContainer = mysql
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(DATABASE_USERNAME)
                    .withPassword(DATABASE_PASSWORD);
        }
    }

    @BeforeAll
    protected void setUp() throws DatabaseConnectionException, DatabaseExecutionException {
        InitScripts initScripts = getInitializationScripts();

        // postgres
        postgresDatabase = createDatabase(postgresContainer, DatabaseEngine.POSTGRES);
        postgresDAO = new PostgresDAO(postgresDatabase);

        if (initScripts.postgresScript != null) {
            postgresDAO.updateDatabase(initScripts.postgresScript);
        }

        // mysql
        mySqlDatabase = createDatabase(mySqlContainer, DatabaseEngine.MYSQL);
        mySqlDAO = new MySqlDAO(mySqlDatabase);

        if (initScripts.mySqlScript != null) {
            for (String script : Arrays.stream(initScripts.mySqlScript.split(COMMAND_SEPARATOR)).toList()) {
                mySqlDAO.updateDatabase(script);
            }
        }
    }

    /**
     * Get scripts for initialization of the databases
     */
    protected abstract InitScripts getInitializationScripts();

    protected Integer getPostgresPort() {
        return postgresContainer.getFirstMappedPort();
    }

    protected Integer getMySqlPort() {
        return mySqlContainer.getFirstMappedPort();
    }

    private Database createDatabase(JdbcDatabaseContainer<?> container, DatabaseEngine engine) {
        return Database.builder()
                .name("Local testing database")
                .host(container.getHost())
                .database(container.getDatabaseName())
                .userName(container.getUsername())
                .password(container.getPassword())
                .port(container.getFirstMappedPort())
                .chats(new ArrayList<>())
                .engine(engine)
                .build();
    }

    /**
     * Scripts for initialization of the databases
     *
     * @param postgresScript commands for Postgres
     * @param mySqlScript    commands for MySQL separated by {@link #COMMAND_SEPARATOR} because MySQL doesn't support
     *                       multiple commands in one query
     */
    public record InitScripts(String postgresScript, String mySqlScript) {

        public static InitScripts mySql(String mySqlScript) {
            return new InitScripts(null, mySqlScript);
        }

        public static InitScripts postgres(String postgresScript) {
            return new InitScripts(postgresScript, null);
        }
    }
}
