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
        return List.of(FileUtils.getFileContent("./src/test/resources/dbInsertScripts/postgresUsers.sql"));
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
    void testQuery() {
        AtomicReference<ResultSet> resultRef = new AtomicReference<>();
        // language=SQL
        assertDoesNotThrow(() -> {
            try (ResultSetWrapper result = databaseDAO.query("SELECT * FROM public.user;")) {
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
    private Integer getUsersCount() throws SQLException, DatabaseConnectionException, DatabaseExecutionException {
        // language=SQL
        String query = "SELECT COUNT(*) FROM public.user;";

        try (ResultSetWrapper result = databaseDAO.query(query)) {
            result.resultSet().next();
            return result.resultSet().getInt("count");
        }
    }

}