package com.janbabak.noqlbackend.repository;

import com.janbabak.noqlbackend.db.Database;

import java.sql.SQLException;

public interface DatabaseInfo {
    Database retrieveSchema() throws SQLException;
}
