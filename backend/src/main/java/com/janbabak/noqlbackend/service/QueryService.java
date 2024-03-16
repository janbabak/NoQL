package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.database.Database;
import com.janbabak.noqlbackend.model.database.DatabaseStructure;
import com.janbabak.noqlbackend.model.database.QueryResponse;
import com.janbabak.noqlbackend.model.database.QueryResponse.QueryResult;
import com.janbabak.noqlbackend.service.api.GptApi;
import com.janbabak.noqlbackend.service.api.QueryApi;
import com.janbabak.noqlbackend.service.database.BaseDatabaseService;
import com.janbabak.noqlbackend.service.database.DatabaseServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.DATABASE;

@Slf4j
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
     * Set pagination in query.
     *
     * @param query    database language query
     * @param page     number of pages (first page has index 0)
     * @param pageSize number of items in one page
     * @param database database object
     * @return database language query with pagination
     */
    public String setPagination(String query, Integer page, Integer pageSize, Database database) {
        // defaults
        if (page == null) {
            page = 0;
        }
        if (pageSize == null) {
            pageSize = 50; // TODO: load default page size from env
        }

        return switch (database.getEngine()) {
            case POSTGRES, MYSQL -> {
                // removes trailing semicolon if it is present
                if (query.charAt(query.length() - 1) == ';') {
                    query = query.substring(0, query.length() - 1);
                }

                // add limit only if it is not present
                if (!query.contains("LIMIT")) {
                    query = "%s\nLIMIT %d".formatted(query, pageSize);
                } else {
                    // verify that the limit is not too big
                    int limitIndex = query.indexOf("LIMIT");
                    String queryAfterLimit = query.substring(limitIndex + "LIMIT ".length());
                    if (queryAfterLimit.isEmpty()) {
                        // TODO: query is in bad syntax
                    }
                    int indexOfCharAfterLimitValue = queryAfterLimit.indexOf(" ");
                    if (indexOfCharAfterLimitValue == -1) {
                        // space after limit value was not found, there might be a semicolon
                        indexOfCharAfterLimitValue = queryAfterLimit.indexOf(";");
                    }
                    if (indexOfCharAfterLimitValue == -1) {
                        indexOfCharAfterLimitValue = queryAfterLimit.length();
                    }

                    String limitValueString = queryAfterLimit.substring(0, indexOfCharAfterLimitValue);
                    int limitValue = Integer.parseInt(limitValueString);
                    int maxAllowedPageSize = 250; // TODO: load from environment variable
                    if (limitValue > maxAllowedPageSize) {
                        log.error("Page size={} greater than maximal allowed value={}",
                                pageSize, maxAllowedPageSize);
                        String queryBeforeLimitValue = query.substring(0, limitIndex + "LIMIT ".length());
                        String queryAfterLimitValue = queryAfterLimit.substring(indexOfCharAfterLimitValue);
                        query = queryBeforeLimitValue + maxAllowedPageSize + queryAfterLimitValue;
                    }

                }

                // add offset only if it is not present
                if (!query.contains("OFFSET")) {
                    query = "%s\nOFFSET %d".formatted(query, page * pageSize);
                }

                yield query + ";";
            }
        };
    }

    /**
     * Execute (natural/query language) query - translate it via LLM to the database specific query language if needed
     * and execute it.
     *
     * @param id                       database id
     * @param query                    in natural query or database query language
     * @param translateToQueryLanguage if true query in the request will be translated via LLM to query language,
     *                                 otherwise it will be executed like it is.
     * @return query result
     * @throws EntityNotFoundException     queried database not found.
     * @throws LLMException                LLM request failed.
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    public QueryResponse executeQuery(
            UUID id,
            String query,
            Boolean translateToQueryLanguage,
            Integer page,
            Integer pageSize
    ) throws Exception {

        log.info("Execute query: query={}, database_id={}, naturalQuery={}.", query, id, translateToQueryLanguage);

        Database database = databaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, id));

        BaseDatabaseService specificDatabaseService = DatabaseServiceFactory.getDatabaseService(database);

        // if query is in natural language, it needs to be translated to query language
        if (translateToQueryLanguage) {
            DatabaseStructure databaseStructure = specificDatabaseService.retrieveSchema();
            String LLMQuery = createQuery(query, databaseStructure.generateCreateScript(), database);
            query = queryApi.queryModel(LLMQuery);
        }

        setPagination(query, page, pageSize, database);

        try {
            QueryResult queryResult = new QueryResult(specificDatabaseService.executeQuery(query));
            return new QueryResponse(queryResult, query);
        } catch (SQLException e) {
            throw new DatabaseExecutionException(e.getMessage());
        }
    }
}
