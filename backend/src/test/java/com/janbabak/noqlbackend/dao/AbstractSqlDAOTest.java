package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractSqlDAOTest extends AbstractDAOTest {

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
