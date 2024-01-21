package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;

import java.sql.ResultSet;

public class BaseDatabaseService {
    protected DatabaseDAO databaseDAO;

    public ResultSet executeQuery(String query) {
        return databaseDAO.query(query);
    }
}
