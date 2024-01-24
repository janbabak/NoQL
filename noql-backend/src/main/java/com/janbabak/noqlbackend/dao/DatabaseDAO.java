package com.janbabak.noqlbackend.dao;

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
     */
    public abstract ResultSet getSchemasTablesColumns();

    /**
     * Retrieve foreign keys.
     *
     * @return query result
     */
    public abstract ResultSet getForeignKeys();

    /**
     * Query the database.
     *
     * @param query query string
     * @return query result
     */
    public ResultSet query(String query) {
        try {
            connect();
            return connection.createStatement().executeQuery(query); // TODO create read only connection
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        } finally {
            disconnect();
        }
        return null;
    }

    /**
     * Test connection to database.
     *
     * @return true if connection established, false otherwise (e.g. bad credentials, etc.)
     */
    @SuppressWarnings("all")
    public abstract boolean testConnection();

    /**
     * Create connection URL for specific database engine.
     *
     * @return connection URL
     */
    protected abstract String createConnectionUrl();

    /**
     * Connect to the database.
     *
     * @throws SQLException TODO
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
            System.out.println(exception.getMessage());
        }
    }

}
