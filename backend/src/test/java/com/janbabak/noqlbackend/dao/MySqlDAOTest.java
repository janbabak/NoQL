package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class MySqlDAOTest extends MySqlTest {

    /**
     * Get list of scripts to create database. Each script is one command in case of MySQL, multiple commands in one
     * script don't work.
     *
     * @return list of commands.
     */
    @Override
    protected List<String> getCreateScript() {
        return Arrays.stream(FileUtils.getFileContent("./src/test/resources/dbInsertScripts/mySqlUsers.sql")
                .split(COMMAND_SEPARATOR)).toList();
    }

    @Test
    @DisplayName("Test create connection URL")
    void testCreateConnectionUrl() {
        String expected = "jdbc:mysql://localhost:" + getDatabasePort() + "/mysql-test-database";
        assertEquals(expected, mysqlDAO.createConnectionUrl());
    }

    @Test
    @DisplayName("Test connection")
    void testConnection() {
        assertDoesNotThrow(() -> mysqlDAO.testConnection());
    }

    @Test
    @DisplayName("Test query database")
    void testQuery() {
        AtomicReference<ResultSet> resultRef = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            try (ResultSetWrapper result = mysqlDAO.query("SELECT * FROM `user`")) {
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
        try (ResultSetWrapper result = mysqlDAO.query(query)) {}
        catch (DatabaseExecutionException e) {}

        assertEquals(22, getUsersCount());
    }

    /**
     * Count users in the database
     *
     * @return number of records in the user table
     */
    private Integer getUsersCount() throws SQLException, DatabaseConnectionException, DatabaseExecutionException {
        try (ResultSetWrapper result = mysqlDAO.query("SELECT COUNT(*) AS count FROM `user`;")) {
            result.resultSet().next();
            return result.resultSet().getInt("count");
        }
    }
}