package com.janbabak.noqlbackend.dao;

import lombok.Data;

import java.sql.*;

/**
 * Database data access object.<br />
 * Used to query user's databases.
 */
@Data
public abstract class DatabaseDAO {
    protected String host;
    protected String port;
    protected String database;
    protected String user;
    protected String password;

    protected Connection connection;

    public DatabaseDAO(String host, String port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        this.connection = null;
    }

    /**
     * It's possible to initialize the object without setting its properties, but it is necessary to set them
     * before establishing the connection.
     */
    public DatabaseDAO() {
        host = null;
        port = null;
        database = null;
        user = null;
        password = null;
        connection = null;
    }

    /**
     * Create connection URL for specific database engine.
     *
     * @return connection URL
     */
    protected abstract String createConnectionUrl();

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
     * Connect to the database.
     *
     * @throws SQLException TODO
     */
    protected void connect() throws SQLException {
        connection = DriverManager.getConnection(
                createConnectionUrl(),
                user, password
        );
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
