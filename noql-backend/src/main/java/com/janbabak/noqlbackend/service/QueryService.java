package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.model.UserQueryRequest;
import com.janbabak.noqlbackend.model.database.Database;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.database.DatabaseStructure;
import com.janbabak.noqlbackend.service.api.GptApi;
import com.janbabak.noqlbackend.service.api.QueryApi;
import com.janbabak.noqlbackend.service.database.DatabaseService;
import com.janbabak.noqlbackend.service.database.DatabaseServiceFactory;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.Locale;

@Service
public class QueryService {
    private final QueryApi queryApi = new GptApi();

    /**
     * Create query content for the LLM.
     *
     * @param naturalLanguageQuery query from the user/customer
     * @param dbStructure          structure of the database, e.g. create script (describes tables, columns, etc.)
     * @param database             database metadata object
     * @return generated query
     */
    public static String createQuery(
            String naturalLanguageQuery, String dbStructure, Database database) {
        StringBuilder queryBuilder = new StringBuilder();

        if (database.getIsSQL()) {
            queryBuilder
                    .append("Write an SQL query for the ")
                    .append(database.getEngine().toString().toLowerCase(Locale.ROOT))
                    .append(" database, which does the following: ");
        } else {
            queryBuilder
                    .append("Write a query for the ")
                    .append(database.getEngine().toString().toLowerCase(Locale.ROOT))
                    .append("database.\n")
                    .append("It should do the following: ");
        }

        queryBuilder
                .append(naturalLanguageQuery)
                .append("\nThis is the database structure:\n")
                .append(dbStructure);

        return queryBuilder.toString();
    }

    public ResultSet handleQuery(UserQueryRequest request) throws Exception {
        // TODO retrieve persistence layer
        Database database = new Database(
                "my-id",
                "localhost",
                "5432",
                "database",
                "user",
                "password",
                DatabaseEngine.POSTGRES,
                true);

        DatabaseService databaseService = DatabaseServiceFactory.getDatabaseService(database);
        DatabaseStructure databaseStructure = databaseService.retrieveSchema();
        String LLMQuery = createQuery(
                request.getNaturalLanguageQuery(),
                databaseStructure.generateCreateScript(),
                database);

        System.out.println(databaseStructure.generateCreateScript());

        String dbLanguageQuery = queryApi.queryModel(LLMQuery);

        System.out.println(dbLanguageQuery);

        return databaseService.executeQuery(dbLanguageQuery);
    }
}
