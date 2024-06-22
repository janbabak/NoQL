package com.janbabak.noqlbackend.dao;

import java.sql.ResultSet;

/**
 * Used because in some cases ResultSet object needs to be returned without closing the connection with database.
 * For example if the connection is closed in MySQL database, ResultSet is inaccessible. This class close the connection
 * when used properly.
 *
 * @param resultSet       ResultSet from the query
 * @param closeConnection Should close are resources including the resultSet, connection, statement, ...
 */
public record ResultSetWrapper(
        ResultSet resultSet,
        Runnable closeConnection
) implements AutoCloseable {

    @Override
    public void close() {
        closeConnection.run();
    }
}
