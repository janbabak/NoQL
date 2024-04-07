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
        connect();

        try {
            log.info("Execute query={}.", query);
            return connection.createStatement().executeQuery(query);
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
     * Create connection URL for specific database engine.
     *
     * @return connection URL
     */
    protected abstract String createConnectionUrl();

    /**
     * Connect to the database.
     *
     * @throws DatabaseConnectionException cannot establish connection with the database
     */
    protected void connect() throws DatabaseConnectionException {
        try {
            connection = DriverManager.getConnection(
                    createConnectionUrl(),
                    databaseMetadata.getUserName(),
                    databaseMetadata.getPassword());
            connection.setReadOnly(true);
        } catch (SQLException e) {
            log.error("Error while connecting to database - message={}.", e.getMessage());
            throw new DatabaseConnectionException(e.getMessage());
        }
    }

    /**
     * Close connection to the database.
     */
    protected void disconnect() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            log.error("Error while disconnecting from database - message={}.", e.getMessage());
        }
    }

}
