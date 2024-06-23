package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class PostgresDAOTest extends LocalDatabaseTest {

    public int getDatabasePort() {
        return getPostgresPort();
    }

    public DatabaseDAO getDatabaseDao() {
        return postgresDAO;
    }

    /**
     * Get scripts for initialization of the databases
     */
    @Override
    protected InitScripts getPostgresInitializationScripts() {
        return InitScripts.postgres(
                FileUtils.getFileContent("./src/test/resources/dbInsertScripts/postgres/eshopUser.sql"));
    }


    @Test
    @DisplayName("Test create connection URL")
    void testCreateConnectionUrl() {
        String expected = "jdbc:postgresql://localhost:" + getDatabasePort() + "/test-database";
        assertEquals(expected, getDatabaseDao().createConnectionUrl());
    }

    @Test
    @DisplayName("Test connection")
    void testConnection() {
        assertDoesNotThrow(() -> getDatabaseDao().testConnection());
    }

    @Test
    @DisplayName("Test query database")
    @SuppressWarnings("all") // IDE can't see the columns
    void testQuery() {
        AtomicReference<ResultSet> resultRef = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            // language=SQL
            try (ResultSetWrapper result = getDatabaseDao().query("SELECT * FROM eshop_user;")) {
                resultRef.set(result.resultSet());
            }
        });
        assertNotNull(resultRef.get());
    }

    @Test
    @DisplayName("Test connection is readOnly")
    @SuppressWarnings("all") // epmty try-catch block
    void testConnectionIsReadyOnly() throws DatabaseConnectionException, DatabaseExecutionException, SQLException {
        assertEquals(22, getUsersCount());

        // language=SQL
        String query = "DELETE FROM eshop_user WHERE age > 0;";
        try (ResultSetWrapper result = getDatabaseDao().query(query)) {}
        catch (DatabaseExecutionException e) {}

        assertEquals(22, getUsersCount());
    }

    /**
     * Count users in the database
     *
     * @return number of records in the user table
     */
    @SuppressWarnings("all") // IDE can't see the columns
    private Integer getUsersCount() throws SQLException, DatabaseConnectionException, DatabaseExecutionException {
        // language=SQL
        String query = "SELECT COUNT(*) AS count FROM eshop_user;";

        try (ResultSetWrapper result = getDatabaseDao().query(query)) {
            result.resultSet().next();
            return result.resultSet().getInt("count");
        }
    }
}