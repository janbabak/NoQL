package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.database.DatabaseCredentialsEncryptionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.*;

/**
 * Database data access object.<br />
 * Used to query user's databases.
 */
@Slf4j
@Data
@Service
@Scope("prototype")
@RequiredArgsConstructor
public abstract class DatabaseDAO {

    private final DatabaseCredentialsEncryptionService encryptionService;

    @Accessors(fluent = true)
    protected Database databaseMetadata = null;

    protected Connection connection = null;

    /**
     * Retrieve database schemas, tables columns and primary keys.
     *
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    public abstract ResultSetWrapper getSchemasTablesColumns()
            throws DatabaseConnectionException, DatabaseExecutionException;

    /**
     * Retrieve foreign keys.
     *
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    public abstract ResultSetWrapper getForeignKeys() throws DatabaseConnectionException, DatabaseExecutionException;

    /**
     * Query the database.
     *
     * @param query query string
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    public ResultSetWrapper query(String query) throws DatabaseConnectionException, DatabaseExecutionException {
        connect(true);

        try {
            log.info("Execute read-only query={}.", query);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            return new ResultSetWrapper(resultSet, () -> {
                try {
                    disconnect();
                    statement.close();
                    resultSet.close();
                } catch (SQLException e) {
                    log.error("Error while closing statement and result set - message={}.", e.getMessage());
                }
            });
        } catch (SQLException e) {
            throw new DatabaseExecutionException(e.getMessage());
        }
    }

    /**
     * Update the database. Not read-only connection.
     *
     * @param query query string
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    void updateDatabase(String query) throws DatabaseConnectionException, DatabaseExecutionException {
        try {
            connect(false);
            log.info("Execute query={}.", query);
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            throw new DatabaseExecutionException(e.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * Test connection to database.
     *
     * @throws DatabaseConnectionException cannot establish connection with the database
     */
    public void testConnection() throws DatabaseConnectionException {
        connect(true);

        if (connection == null) {
            throw new DatabaseConnectionException();
        }
    }

    /**
     * Close connection to the database.
     */
    public void disconnect() {
        if (this.connection == null) {
            return;
        }
        try {
            this.connection.close();
        } catch (SQLException e) {
            log.error("Error while disconnecting from database - message={}.", e.getMessage());
        }
    }

    /**
     * Create connection URL for specific database engine.
     *
     * @return connection URL
     */
    protected abstract String createConnectionUrl();

    /**
     * Connect to the database.
     *
     * @param readOnly if true, the connection is read-only
     * @throws DatabaseConnectionException cannot establish connection with the database
     */
    protected void connect(Boolean readOnly) throws DatabaseConnectionException {
        try {
            connection = DriverManager.getConnection(
                    createConnectionUrl(),
                    databaseMetadata.getUserName(),
                    encryptionService.decryptCredentials(databaseMetadata.getPassword()));
            connection.setReadOnly(readOnly);
            connection.setAutoCommit(!readOnly);
        } catch (SQLException e) {
            log.error("Error while connecting to database - message={}.", e.getMessage());
            throw new DatabaseConnectionException(e.getMessage());
        }
    }
}
