package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.MySqlDAO;
import com.janbabak.noqlbackend.dao.PostgresDAO;
import com.janbabak.noqlbackend.model.entity.Database;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseServiceFactory {

    private final PostgresDAO postgresDAO;
    private final MySqlDAO mySqlDAO;
    private final PostgresService postgresService;
    private final MySqlService mySqlService;

    /**
     * Get database service based on the database engine.
     *
     * @param database database metadata.
     * @return correct database service
     */
    public @NotNull BaseDatabaseService getDatabaseService(@NotNull Database database) {
        return switch (database.getEngine()) {
            case POSTGRES -> postgresService.setDatabaseDaoMetadata(database);
            case MYSQL -> mySqlService.setDatabaseDaoMetadata(database);
        };
    }

    /**
     * Get database DAO based on the database engine.
     *
     * @param database database metadata
     * @return correct DAO
     */
    public @NotNull DatabaseDAO getDatabaseDAO(@NotNull Database database) {
        return switch (database.getEngine()) {
            case POSTGRES -> postgresDAO.databaseMetadata(database);
            case MYSQL -> mySqlDAO.databaseMetadata(database);
        };
    }
}
