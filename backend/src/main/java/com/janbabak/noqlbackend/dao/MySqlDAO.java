package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.entity.Database;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.ResultSet;

@Data
@EqualsAndHashCode(callSuper = true)
public class MySqlDAO extends DatabaseDAO {

    public MySqlDAO(Database database) {
        super(database);
    }

    /**
     * Retrieve database schemas, tables columns and primary keys.
     *
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    @Override
    public ResultSet getSchemasTablesColumns() throws DatabaseConnectionException, DatabaseExecutionException {
        return null;
    }

    /**
     * Retrieve foreign keys.
     *
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    @Override
    public ResultSet getForeignKeys() throws DatabaseConnectionException, DatabaseExecutionException {
        return null;
    }

    /**
     * Test connection to database.
     *
     * @throws DatabaseConnectionException cannot establish connection with the database
     */
    @Override
    public void testConnection() throws DatabaseConnectionException {
        connect(true);

        if (connection == null) {
            throw new DatabaseConnectionException();
        } // TODO: move to ancestor
    }

    /**
     * Create connection URL for specific database engine.
     *
     * @return connection URL
     */
    @Override
    protected String createConnectionUrl() {
        return "jdbc:mysql://" + databaseMetadata.getHost() + ":" + databaseMetadata.getPort() +
                "/" + databaseMetadata.getDatabase(); // TODO: formatted string
    }
}
