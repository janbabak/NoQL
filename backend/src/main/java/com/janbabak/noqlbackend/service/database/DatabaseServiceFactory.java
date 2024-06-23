package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.MySqlDAO;
import com.janbabak.noqlbackend.dao.PostgresDAO;
import com.janbabak.noqlbackend.model.entity.Database;
import jakarta.validation.constraints.NotNull;

public class DatabaseServiceFactory {

    /**
     * Get database service based on the database engine.
     *
     * @param database database metadata.
     * @return correct database service
     */
    public static @NotNull BaseDatabaseService getDatabaseService(@NotNull Database database) {
        return switch (database.getEngine()) {
            case POSTGRES -> new PostgresService(database);
            case MYSQL -> new MySqlService(database);
        };
    }

    /**
     * Get database DAO based on the database engine.
     *
     * @param database database metadata
     * @return correct DAO
     */
    public static @NotNull DatabaseDAO getDatabaseDAO(@NotNull Database database) {
        return switch (database.getEngine()) {
            case POSTGRES -> new PostgresDAO(database);
            case MYSQL -> new MySqlDAO(database);
        };
    }
}
