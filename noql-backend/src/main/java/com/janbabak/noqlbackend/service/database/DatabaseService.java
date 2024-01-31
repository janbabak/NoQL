package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.model.database.DatabaseStructure;

import java.sql.ResultSet;

public interface DatabaseService {

    /**
     * Retrieves database schema (structure of tables, columns, etc...)
     *
     * @return database structure
     * @throws DatabaseConnectionException cannot establish connection with the database, syntax error, ...
     */
    DatabaseStructure retrieveSchema() throws DatabaseConnectionException;

    /**
     * Execute SQL or some other query (depends on the underling database engine)
     *
     * @param query query string
     * @return result set
     * @throws DatabaseConnectionException cannot establish connection with the database, syntax error, ...
     */
    ResultSet executeQuery(String query) throws DatabaseConnectionException;
}
