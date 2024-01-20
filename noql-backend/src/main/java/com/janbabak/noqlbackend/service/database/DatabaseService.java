package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.model.database.Database;

import java.sql.SQLException;

public interface DatabaseService {
    Database retrieveSchema() throws SQLException;
}
