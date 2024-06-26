package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Database;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class for testing classes that access databases.<br />
 * It uses Testcontainers to run databases in Docker container.<br />
 * It doesn't change the Spring profile, so the database is not used by ORM repositories.
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocalDatabaseTest {
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
        postgresDatabase = createDatabase(postgresContainer, DatabaseEngine.POSTGRES);
        postgresDAO = new PostgresDAO(postgresDatabase);

        mySqlDatabase = createDatabase(mySqlContainer, DatabaseEngine.MYSQL);
        mySqlDAO = new MySqlDAO(mySqlDatabase);

        Scripts initScripts = getInitializationScripts();
        if (initScripts == null) {
            return;
        }

        executeScripts(initScripts);
    }

    @AfterAll
    protected void tearDown() throws DatabaseConnectionException, DatabaseExecutionException {
        Scripts cleanupScript = getCleanupScript();
        if (cleanupScript == null) {
            return;
        }

        executeScripts(cleanupScript);
    }

    /**
     * Execute in all databases if scripts are provided.
     *
     * @param scripts scripts to execute
     * @throws DatabaseConnectionException cannot establish connection to the database
     * @throws DatabaseExecutionException  cannot execute the script - syntax error
     */
    private void executeScripts(Scripts scripts) throws DatabaseConnectionException, DatabaseExecutionException {
        if (scripts.postgresScript != null) {
            postgresDAO.updateDatabase(scripts.postgresScript);
        }

        if (scripts.mySqlScript != null) {
            for (String command : Arrays.stream(scripts.mySqlScript.split(COMMAND_SEPARATOR)).toList()) {
                mySqlDAO.updateDatabase(command);
            }
        }
    }

    /**
     * Get scripts for initialization of the databases
     */
    protected Scripts getInitializationScripts() {
        return null;
    }

    /**
     * Get scripts for cleanup of the databases.
     */
    protected Scripts getCleanupScript() {
        return null;
    }

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
    public record Scripts(String postgresScript, String mySqlScript) {

        public static Scripts mySql(String mySqlScript) {
            return new Scripts(null, mySqlScript);
        }

        public static Scripts postgres(String postgresScript) {
            return new Scripts(postgresScript, null);
        }
    }
}
