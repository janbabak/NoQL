package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.model.database.DatabaseStructure;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseService {
    DatabaseStructure retrieveSchema() throws SQLException;

    /**
     * Execute SQL or some other query (depends on the underling database engine)
     * @param query query string
     * @return result set
     */
    ResultSet executeQuery(String query);
}
