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
     * @return database language query with pagination
     * @throws BadRequestException        value is greater than maximum allowed value
     */
    public String setPaginationInSqlQuery(String query, Integer page, Integer pageSize, Database database)
            throws BadRequestException {
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
            case POSTGRES, MYSQL -> "SELECT * FROM (%s) LIMIT %d OFFSET %d;"
                    .formatted(trimAndRemoveTrailingSemicolon(query), pageSize, page * pageSize);
        };
    }

    private String trimAndRemoveTrailingSemicolon(String query) {
        query = query.trim();

        // removes trailing semicolon if it is present
        return query.charAt(query.length() - 1) == ';'
            ? query.substring(0, query.length() - 1)
            : query;
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

        selectQuery = trimAndRemoveTrailingSemicolon(selectQuery);
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
     * @return query result
     * @throws EntityNotFoundException     queried database not found.
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws BadRequestException         requested page size is greater than maximum allowed value
     */
    public QueryResponse executeQueryLanguageSelectQuery(
            UUID id,
            String query,
            Integer page,
            Integer pageSize
    ) throws EntityNotFoundException, DatabaseConnectionException, BadRequestException {

        log.info("Execute query language query: query={}, database_id={}.", query, id);

        Database database = databaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, id));

        BaseDatabaseService specificDatabaseService = DatabaseServiceFactory.getDatabaseService(database);

        String paginatedQuery = setPaginationInSqlQuery(query, page, pageSize, database);
        try {
            QueryResult queryResult = new QueryResult(specificDatabaseService.executeQuery(paginatedQuery));
            Long totalCount = getTotalCount(query, specificDatabaseService);

            return QueryResponse.successfulResponse(queryResult, query, totalCount);
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
            query = queryApi.queryModel(LLMQuery);
            // TODO: remove after testing
//            query = """
//                    ```
//                    select * from user;
//                    ```""";
            String paginatedQuery = setPaginationInSqlQuery(query, 0, pageSize, database);
            try {
                QueryResult queryResult = new QueryResult(specificDatabaseService.executeQuery(paginatedQuery));
                Long totalCount = getTotalCount(query, specificDatabaseService);

                return QueryResponse.successfulResponse(queryResult, query, totalCount);
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
