package com.janbabak.noqlbackend.dao;

import java.sql.ResultSet;

/**
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
