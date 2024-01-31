package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.LLMException;
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

import java.sql.SQLException;
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
     *
     * @param request from the API
     * @return query result
     * @throws EntityNotFoundException queried database not found.
     * @throws LLMException            LLM request failed.
     * @throws DatabaseConnectionException cannot establish connection with the database, syntax error, ...
     */
    public QueryResponse handleQuery(QueryRequest request)
            throws EntityNotFoundException, LLMException, DatabaseConnectionException {
        Optional<Database> optionalDatabase = databaseRepository.findById(request.getDatabaseId());

        if (optionalDatabase.isEmpty()) {
            throw new EntityNotFoundException(EntityNotFoundException.Entity.DATABASE, request.getDatabaseId());
        }

        DatabaseService specificDatabaseService = DatabaseServiceFactory.getDatabaseService(optionalDatabase.get());
        DatabaseStructure databaseStructure = specificDatabaseService.retrieveSchema();

        String LLMQuery = createQuery(
                request.getNaturalLanguageQuery(),
                databaseStructure.generateCreateScript(),
                optionalDatabase.get());
        String generatedQuery = queryApi.queryModel(LLMQuery);

        try {
            QueryResult queryResult = new QueryResult(specificDatabaseService.executeQuery(generatedQuery));
            return new QueryResponse(queryResult, generatedQuery);
        } catch (SQLException exception) {
            throw new DatabaseConnectionException(exception.getMessage());
        }
    }
}
