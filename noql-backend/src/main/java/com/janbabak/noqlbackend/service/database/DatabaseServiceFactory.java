package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.model.database.Database;

public class DatabaseServiceFactory {
    private static DatabaseService postgresService = null;

    /**
     * Get database service based on the database engine.
     * @param database database metadata.
     * @return correct database service
     */
    public static DatabaseService getDatabaseService(Database database) {
        return switch (database.getEngine()) {
            case POSTGRES -> {
                if (postgresService == null) {
                    postgresService = new PostgresService(database);
                }
                yield postgresService;
            }
            case MYSQL -> null; // TODO implement
        };
    }
}
