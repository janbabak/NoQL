package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.Settings;
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
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
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

    @Autowired
    private Settings settings;

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
     * Set pagination in SQL query using {@code LIMIT} and {@code OFFSET}.
     *
     * @param query         database language query
     * @param page          number of pages (first page has index 0), if null, default value is 0
     * @param pageSize      number of items in one page,<br />
     *                      if null default value is defined by {@code PAGINATION_DEFAULT_PAGE_SIZE} env,<br />
     *                      max allowed size is defined by {@code PAGINATION_MAX_PAGE_SIZE} env
     * @param database      database object
     * @param setOffset     if true set the offset value, otherwise ignore offset
     * @param overrideLimit if true override the extracted limit value from the query no matter what by value
     * @return database language query with pagination
     * @throws BadRequestException        value is greater than maximum allowed value
     * @throws DatabaseExecutionException SQL query is in bad syntax
     */
    public String setPaginationInSqlQuery(
            String query,
            Integer page,
            Integer pageSize,
            Database database,
            Boolean setOffset,
            Boolean overrideLimit
    ) throws BadRequestException, DatabaseExecutionException {
        // defaults
        if (page == null) {
            page = 0;
        }
        if (pageSize == null) {
            pageSize = settings.defaultPageSize;
        }
        if (pageSize > settings.maxPageSize) {
            log.error("Page size={} greater than maximal allowed value={}", pageSize, settings.maxPageSize);
            throw new BadRequestException("Page size is greater than maximum allowed value.");
        }

        query = query.trim();

        return switch (database.getEngine()) {
            case POSTGRES, MYSQL -> {
                query = setValueOfPropertyInSqlQuery(
                        query, "LIMIT", pageSize, settings.maxPageSize, overrideLimit);

                if (setOffset) {
                    int offsetValue = pageSize * page;
                    query = setValueOfPropertyInSqlQuery(
                            query, "OFFSET", offsetValue, null, true);
                }
                yield query;
            }
        };
    }

    /**
     * Set value of property in the SQL query.
     *
     * @param query               SQL query
     * @param property            e.g. {@code LIMIT}, {@code OFFSET}
     * @param value               value of the property
     * @param maximumAllowedValue if value is greater than maximum allowed value, maximum allowed value is used instead
     * @param replaceEveryTime    if true value of the property is replaced in the query every time, if false value
     *                            extracted from the query (if present) is replaced by value only if the value is lower
     *                            than the extracted one.
     * @return modified query
     * @throws DatabaseExecutionException query execution failed (syntax error - property has no value)
     */
    private String setValueOfPropertyInSqlQuery(
            String query,
            String property,
            Integer value,
            Integer maximumAllowedValue,
            Boolean replaceEveryTime
    ) throws DatabaseExecutionException {
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
            log.error("Property={} has no value, query={}", property, query);
            throw new DatabaseExecutionException("Property has no value");
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
     * Get total number of rows that SQL select query returns.
     *
     * @param selectQuery     select statement
     * @param databaseService service that can handle the query
     * @return total number of rows
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    private Long getTotalCount(String selectQuery, BaseDatabaseService databaseService)
            throws DatabaseConnectionException, DatabaseExecutionException {

        selectQuery = selectQuery.trim();
        // remove trailing semicolon if it is present
        if (selectQuery.charAt(selectQuery.length() - 1) == ';') {
            selectQuery = selectQuery.substring(0, selectQuery.length() - 1);
        }

        String selectCountQuery = "SELECT COUNT(*) AS count from (%s);".formatted(selectQuery);

        ResultSet resultSet = databaseService.executeQuery(selectCountQuery);

        try {
            return resultSet.next() ? resultSet.getLong(1) : null;
        } catch (SQLException e) {
            throw new DatabaseExecutionException("Cannot parse total count value from query");
        }
    }

    /**
     * Execute query language select query.
     * Select query is read only, and it returns a result that is automatically paginated.
     *
     * @param id                       database id
     * @param query                    in natural query or database query language
     * @param page                     page number (fist page starts by 0), if null, default value is 0
     * @param pageSize                 number of items in one page,<br />
     *                                 default value is defined by {@code PAGINATION_DEFAULT_PAGE_SIZE} env,<br />
     *                                 max allowed size is defined by {@code PAGINATION_MAX_PAGE_SIZE} env
     * @param overrideLimit            if true overrides the limit value from the query to the pageSize
     * @return query result
     * @throws EntityNotFoundException     queried database not found.
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws BadRequestException         requested page size is greater than maximum allowed value
     */
    public QueryResponse executeQueryLanguageSelectQuery(
            UUID id,
            String query,
            Integer page,
            Integer pageSize,
            Boolean overrideLimit
    ) throws EntityNotFoundException, DatabaseConnectionException, BadRequestException {

        log.info("Execute query language query: query={}, database_id={}.", query, id);

        Database database = databaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, id));

        BaseDatabaseService specificDatabaseService = DatabaseServiceFactory.getDatabaseService(database);

        try {
            String paginatedQuery = setPaginationInSqlQuery(
                    query, page, pageSize, database, true, overrideLimit);
            QueryResult queryResult = new QueryResult(specificDatabaseService.executeQuery(paginatedQuery));
            Long totalCount = getTotalCount(query, specificDatabaseService);

            return QueryResponse.successfulResponse(queryResult, paginatedQuery, totalCount);
        } catch (DatabaseExecutionException | SQLException e) {
            return QueryResponse.failedResponse(query, e.getMessage());
        }
    }

    /**
     * Execute natural language select query. The query is translated to specific dialect via LLM and then executed.
     * Select query is read only, and it returns a result that is automatically paginated starting by page 0.
     *
     * @param id                       database id
     * @param query                    in natural query or database query language
     * @param pageSize                 number of items in one page,<br />
     *                                 default value is defined by {@code PAGINATION_DEFAULT_PAGE_SIZE} env,<br />
     *                                 max allowed size is defined by {@code PAGINATION_MAX_PAGE_SIZE} env
     * @return query result
     * @throws EntityNotFoundException     queried database not found.
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws BadRequestException         requested page size is greater than maximum allowed value
     */
    public QueryResponse executeSelectNaturalQuery(UUID id, String query, Integer pageSize)
            throws EntityNotFoundException, DatabaseConnectionException, DatabaseExecutionException,
            BadRequestException, LLMException {

        log.info("Execute natural language query: query={}, database_id={}", query, id);

        Database database = databaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, id));

        BaseDatabaseService specificDatabaseService = DatabaseServiceFactory.getDatabaseService(database);
        DatabaseStructure databaseStructure = specificDatabaseService.retrieveSchema();
        String LLMQuery = createQuery(query, databaseStructure.generateCreateScript(), database);

        // executes the query with retires - if it fails translate it via LLM and trie again
        for (int attempt = 1; attempt <= settings.translationRetries; attempt++) {
//            query = queryApi.queryModel(LLMQuery);
            // TODO: remove after testing
            query = """
                    ```
                    select * from user;
                    ```""";
            String paginatedQuery = setPaginationInSqlQuery(
                    query, 0, pageSize, database, false, false);
            try {
                QueryResult queryResult = new QueryResult(specificDatabaseService.executeQuery(paginatedQuery));
                Long totalCount = getTotalCount(query, specificDatabaseService);

                return QueryResponse.successfulResponse(queryResult, paginatedQuery, totalCount);
            } catch (DatabaseExecutionException | SQLException e) {
                log.info("Executing natural language query failed, attempt={}, paginatedQuery={}",
                        attempt, paginatedQuery);
                if (attempt == settings.translationRetries) {
                    return QueryResponse.failedResponse(query, e.getMessage()); // last try failed
                }
            }
        }
        return null;
    }
}
