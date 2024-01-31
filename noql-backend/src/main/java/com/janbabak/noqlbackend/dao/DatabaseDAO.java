package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.model.database.Database;
import lombok.Data;

import java.sql.*;

/**
 * Database data access object.<br />
 * Used to query user's databases.
 */
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
     * @throws DatabaseConnectionException cannot establish connection with the database, syntax error, ...
     */
    public abstract ResultSet getSchemasTablesColumns() throws DatabaseConnectionException;

    /**
     * Retrieve foreign keys.
     *
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database, syntax error, ...
     */
    public abstract ResultSet getForeignKeys() throws DatabaseConnectionException;

    /**
     * Query the database.
     *
     * @param query query string
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database, syntax error, ...
     */
    public ResultSet query(String query) throws DatabaseConnectionException {
        try {
            connect();
            return connection.createStatement().executeQuery(query); // TODO create read only connection
        } catch (SQLException exception) {
            throw new DatabaseConnectionException(exception.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * Test connection to database.
     *
     * @throws DatabaseConnectionException if connection failed (e.g. bad credentials, database not available, ...)
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
     * @throws SQLException cannot establish connection with the database
     */
    protected void connect() throws SQLException {
        connection = DriverManager.getConnection(
                createConnectionUrl(),
                databaseMetadata.getUserName(),
                databaseMetadata.getPassword());
    }

    /**
     * Close connection to the database.
     */
    protected void disconnect() {
        try {
            this.connection.close();
        } catch (SQLException exception) {
            System.out.println(exception.getMessage()); // TODO: log
        }
    }

}
