package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.model.QueryRequest;
import com.janbabak.noqlbackend.model.database.Database;
import com.janbabak.noqlbackend.model.database.DatabaseStructure;
import com.janbabak.noqlbackend.model.database.QueryResponse;
import com.janbabak.noqlbackend.model.database.QueryResponse.QueryResult;
import com.janbabak.noqlbackend.service.api.GptApi;
import com.janbabak.noqlbackend.service.api.QueryApi;
import com.janbabak.noqlbackend.service.database.DatabaseService;
import com.janbabak.noqlbackend.service.database.DatabaseServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueryService {
    private final QueryApi queryApi = new GptApi();
    private final DatabaseRepository databaseRepository;

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

    /**
     * Handle user's query - translate it via LLM to the database specific query language and execute it.
     * @param request from the API
     * @return query result
     * @throws Exception when something went wrong.
     */
    public QueryResponse handleQuery(QueryRequest request) throws Exception {
        Optional<Database> optionalDatabase = databaseRepository.findById(request.getDatabaseId());

        if (optionalDatabase.isEmpty()) {
            throw new Exception("database not found"); // TODO: better exception
        }

        DatabaseService specificDatabaseService = DatabaseServiceFactory.getDatabaseService(optionalDatabase.get());
        DatabaseStructure databaseStructure = specificDatabaseService.retrieveSchema();

        String LLMQuery = createQuery(
                request.getNaturalLanguageQuery(),
                databaseStructure.generateCreateScript(),
                optionalDatabase.get());
        String generatedQuery = queryApi.queryModel(LLMQuery);

        QueryResult queryResult = new QueryResult(specificDatabaseService.executeQuery(generatedQuery));
        return new QueryResponse(queryResult, generatedQuery);
    }
}
