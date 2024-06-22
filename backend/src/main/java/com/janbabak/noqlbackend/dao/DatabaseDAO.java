package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.entity.Database;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

/**
 * Database data access object.<br />
 * Used to query user's databases.
 */
@Slf4j
@Data
public abstract class DatabaseDAO {
    protected Database databaseMetadata;

    protected Connection connection;

    public DatabaseDAO(Database database) {
        this.databaseMetadata = database;
        this.connection = null;
    }

    /**
     * It's possible to initialize the object without setting its properties, but it is necessary to set them
     * before establishing the connection.
     */
    public DatabaseDAO() {
        databaseMetadata = new Database();
        connection = null;
    }

    /**
     * Retrieve database schemas, tables columns and primary keys.
     *
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException query execution failed (syntax error)
     */
    public abstract ResultSet getSchemasTablesColumns() throws DatabaseConnectionException, DatabaseExecutionException;

    /**
     * Retrieve foreign keys.
     *
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException query execution failed (syntax error)
     */
    public abstract ResultSet getForeignKeys() throws DatabaseConnectionException, DatabaseExecutionException;

    /**
     * Query the database.
     *
     * @param query query string
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException query execution failed (syntax error)
     */
    public ResultSet query(String query) throws DatabaseConnectionException, DatabaseExecutionException {
        connect(true);

        try {
            log.info("Execute read-only query={}.", query);
            return connection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            throw new DatabaseExecutionException(e.getMessage());
        }
    }

    /**
     * Update the database. Not read-only connection.
     *
     * @param query query string
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException query execution failed (syntax error)
     */
    void updateDatabase(String query) throws DatabaseConnectionException, DatabaseExecutionException {
        try {
            connect(false);
            log.info("Execute query={}.", query);
            connection.createStatement().executeUpdate(query);
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
    @SuppressWarnings("all")
    public abstract void testConnection() throws DatabaseConnectionException;

    /**
     * Close connection to the database.
     */
    public void disconnect() {
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
                    createConnectionUrl(), databaseMetadata.getUserName(), databaseMetadata.getPassword());
            connection.setReadOnly(readOnly);
            connection.setAutoCommit(!readOnly);
        } catch (SQLException e) {
            log.error("Error while connecting to database - message={}.", e.getMessage());
            throw new DatabaseConnectionException(e.getMessage());
        }
    }
}
