package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class PostgresDAOTest extends PostgresTest {

    @Test
    @DisplayName("Test create connection URL")
    void testCreateConnectionUrl() {
        String expected = "jdbc:postgresql://localhost:5433/test-database";
        assertEquals(expected, postgresDAO.createConnectionUrl());
    }

    @Test
    @DisplayName("Test connection")
    void testConnection() {
        assertDoesNotThrow(() -> postgresDAO.testConnection());
    }

    @Test
    @DisplayName("Test query database")
    void testQuery() {
        AtomicReference<ResultSet> resultRef = new AtomicReference<>();
        // language=SQL
        assertDoesNotThrow(() -> resultRef.set(postgresDAO.query("SELECT * FROM public.user;")));
        assertNotNull(resultRef.get());
    }

    @Test
    @DisplayName("Test connection is readOnly")
    void testConnectionIsReadyOnly() throws DatabaseConnectionException, DatabaseExecutionException, SQLException {
        assertEquals(3, getUsersCount());

        // try to delete users from read-only connection
        try {
            // language=SQL
            postgresDAO.query("DELETE FROM public.user WHERE age > 0;");
        } catch (DatabaseExecutionException e) {
            // do nothing
        }

        assertEquals(3, getUsersCount());
    }

    /**
     * Count users in the database
     *
     * @return number of records in the user table
     */
    private Integer getUsersCount() throws SQLException, DatabaseConnectionException, DatabaseExecutionException {
        // language=SQL
        ResultSet resultSet = postgresDAO.query("SELECT COUNT(*) FROM public.user;");
        resultSet.next();
        return resultSet.getInt("count");
    }

    @Override
    protected String getCreateScript() {
        // language=SQL
        return """
                CREATE TABLE IF NOT EXISTS "user"
                (
                    id         SERIAL PRIMARY KEY,
                    name       VARCHAR(100),
                    age        INTEGER,
                    sex        CHAR(10),
                    email      VARCHAR(100),
                    created_at TIMESTAMP DEFAULT NOW()
                );
                  INSERT INTO "user" (name, age, sex, email)
                  VALUES ('John Doe', 25, 'M', 'john.doe@example.com'),
                         ('Jane Smith', 30, 'F', 'jane.smith@example.com'),
                         ('Jane Doe', 28, 'F', 'jane.doe@example.com');""";
    }
}