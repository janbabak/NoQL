package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.PostgresDAO;
import com.janbabak.noqlbackend.model.database.Database;

public class DatabaseServiceFactory {

    /**
     * Get database service based on the database engine.
     *
     * @param database database metadata.
     * @return correct database service
     */
    public static DatabaseService getDatabaseService(Database database) {
        return switch (database.getEngine()) {
            case POSTGRES -> new PostgresService(database);
            case MYSQL -> null; // TODO: implement
        };
    }

    /**
     * Get database DAO based on the database engine.
     *
     * @param database database metadata
     * @return correct DAO
     */
    public static DatabaseDAO getDatabaseDAO(Database database) {
        return switch (database.getEngine()) {
            case POSTGRES -> new PostgresDAO(database);
            case MYSQL -> null; // TODO: implement
        };
    }
}
