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
     * @param query     database language query
     * @param page      number of pages (first page has index 0), if null, default value is 0
     * @param pageSize  number of items in one page, if null default value is 50, max allowed size is 250 TODO: load from env
     * @param database  database object
     * @param setOffset if true set the offset value, otherwise ignore offset
     * @return database language query with pagination
     */
    public String setPagination(
            String query,
            Integer page,
            Integer pageSize,
            Database database,
            Boolean setOffset
    ) throws Exception {
        // defaults
        if (page == null) {
            page = 0;
        }
        if (pageSize == null) {
            pageSize = 50; // TODO: load default page size from env
        }
        if (pageSize > 250) {
            log.error("Page size={} greater than maximal allowed value={}", pageSize, 250);
            throw new Exception("page size more than allowed");
        }

        query = query.trim();

        return switch (database.getEngine()) {
            case POSTGRES, MYSQL -> {
                query = setValueOfProperty(
                        query,
                        "LIMIT",
                        pageSize,
                        250,
                        false
                );
                if (setOffset) {
                    query = setValueOfProperty(
                            query,
                            "OFFSET",
                            pageSize * page,
                            null,
                            true);
                }
                yield query;
            }
        };
    }

    /**
     * Set value of property in the query
     *
     * @param query               SQL query
     * @param property            e.g. "LIMIT", "OFFSET"
     * @param value               value of the property
     * @param maximumAllowedValue if value is greater than maximum allowed value, maximum allowed value is used
     * @param replaceEveryTime    if true value of the property is replaced in the query every time, if false value
     *                            extracted from the query (if present) is replaced by value only if the value is lower
     * @return modified query
     */
    private String setValueOfProperty(
            String query,
            String property,
            Integer value,
            Integer maximumAllowedValue,
            Boolean replaceEveryTime
    ) {
        if (maximumAllowedValue != null && value > maximumAllowedValue) {
            log.error("Value={} is greater than maximum allowed value={}", value, maximumAllowedValue);
            value = maximumAllowedValue;
        }

        // removes trailing semicolon if it is present
        if (query.charAt(query.length() - 1) == ';') {
            query = query.substring(0, query.length() - 1);
        }

        // add limit if is not present
        if (!query.contains(property)) {
            return "%s\n%s %d;".formatted(query, property, value);
        }

        int propertyIndex = query.indexOf(property);

        String queryAfterProperty = query.substring(propertyIndex + (property + " ").length());

        if (queryAfterProperty.isEmpty()) {
            // TODO: query is in bad syntax
        }

        int indexOfCharAfterPropertyValue = queryAfterProperty.indexOf(" ");
        int indexOfCharCloserToPropertyValue = queryAfterProperty.indexOf("\n");
        if (indexOfCharAfterPropertyValue == -1) {
            indexOfCharAfterPropertyValue = indexOfCharCloserToPropertyValue;
        } else if (indexOfCharCloserToPropertyValue != -1
                && indexOfCharCloserToPropertyValue < indexOfCharAfterPropertyValue) {
            // new line character is closer than space
            indexOfCharAfterPropertyValue = indexOfCharCloserToPropertyValue;
        }

        indexOfCharCloserToPropertyValue = queryAfterProperty.indexOf(";");
        if (indexOfCharAfterPropertyValue == -1) {
            indexOfCharAfterPropertyValue = indexOfCharCloserToPropertyValue;
        } else if (indexOfCharCloserToPropertyValue != -1
                && indexOfCharCloserToPropertyValue < indexOfCharAfterPropertyValue) {
            // semicolon character is closer than space
            indexOfCharAfterPropertyValue = indexOfCharCloserToPropertyValue;
        }

        if (indexOfCharAfterPropertyValue == -1) {
            indexOfCharAfterPropertyValue = queryAfterProperty.length();
        }

        String propertyValueString = queryAfterProperty.substring(0, indexOfCharAfterPropertyValue);
        int extractedPropertyValue = Integer.parseInt(propertyValueString);

        // replace the value only if it is lower than extracted value
        if (!replaceEveryTime && extractedPropertyValue < value) {
            value = extractedPropertyValue;
        }

        if (maximumAllowedValue != null && value > maximumAllowedValue) {
            log.error("Value={} is greater than maximum allowed value={}", value, maximumAllowedValue);
            value = maximumAllowedValue;
        }

        String queryBeforePropertyValue = query.substring(0, propertyIndex + (property + " ").length());
        String queryAfterPropertyValue = queryAfterProperty.substring(indexOfCharAfterPropertyValue);

        return queryBeforePropertyValue + value + queryAfterPropertyValue + ";";
    }

    /**
     * Execute (natural/query language) query - translate it via LLM to the database specific query language if needed
     * and execute it. Automatically set pagination.
     *
     * @param id                       database id
     * @param query                    in natural query or database query language
     * @param translateToQueryLanguage if true query in the request will be translated via LLM to query language,
     *                                 otherwise it will be executed like it is.
     * @param page                     page number (fist page starts by 0), if null, default value is 0
     * @param pageSize                 number of items in one page, if null default value is 50, max allowed size is 250 TODO: load from env
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

        query = setPagination(query, page, pageSize, database, !translateToQueryLanguage);

        try {
            QueryResult queryResult = new QueryResult(specificDatabaseService.executeQuery(query));
            return new QueryResponse(queryResult, query);
        } catch (SQLException e) {
            throw new DatabaseExecutionException(e.getMessage());
        }
    }
}
