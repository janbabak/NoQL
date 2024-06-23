package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class PostgresDAOTest extends PostgresTest {

    @Override
    protected List<String> getInitializationScripts() {
        return List.of(FileUtils.getFileContent("./src/test/resources/dbInsertScripts/postgres/eshopUser.sql"));
    }

    @Test
    @DisplayName("Test create connection URL")
    void testCreateConnectionUrl() {
        String expected = "jdbc:postgresql://localhost:" + getDatabasePort() + "/test-database";
        assertEquals(expected, databaseDAO.createConnectionUrl());
    }

    @Test
    @DisplayName("Test connection")
    void testConnection() {
        assertDoesNotThrow(() -> databaseDAO.testConnection());
    }

    @Test
    @DisplayName("Test query database")
    @SuppressWarnings("all") // IDE can't see the columns
    void testQuery() {
        AtomicReference<ResultSet> resultRef = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            // language=SQL
            try (ResultSetWrapper result = databaseDAO.query("SELECT * FROM eshop_user;")) {
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
        String query = "DELETE FROM public.user WHERE age > 0;";
        try (ResultSetWrapper result = databaseDAO.query(query)) {}
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

        try (ResultSetWrapper result = databaseDAO.query(query)) {
            result.resultSet().next();
            return result.resultSet().getInt("count");
        }
    }

}