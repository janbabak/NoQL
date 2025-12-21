package com.janbabak.noqlbackend.service.langChain;

import com.janbabak.noqlbackend.dao.ResultSetWrapper;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.query.RetrievedData;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.QueryService.PaginatedQuery;
import com.janbabak.noqlbackend.service.database.BaseDatabaseService;
import com.janbabak.noqlbackend.service.database.DatabaseServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

import static com.janbabak.noqlbackend.service.QueryService.getTotalCount;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentalQueryService {

    private final DatabaseServiceFactory databaseServiceFactory;


    public RetrievedData executeQuery(String query, Database database, int page, int pageSize)
            throws BadRequestException, DatabaseConnectionException, DatabaseExecutionException, SQLException {

        validateQuery(query);

        PaginatedQuery paginatedQuery = setPaginationInSqlQuery(query, page, pageSize, database);
        BaseDatabaseService databaseService = databaseServiceFactory.getDatabaseService(database);

        try (ResultSetWrapper result = databaseService.executeQuery(paginatedQuery.query())) {
            Long totalCount = getTotalCount(query, databaseService);
            return new RetrievedData(result.resultSet(), paginatedQuery.page(), paginatedQuery.pageSize(), totalCount);
        }
    }

    private void validateQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be null or blank");
        }
    }

    /**
     * Set pagination in SQL query using {@code LIMIT} and {@code OFFSET}.
     *
     * @param query    database language query
     * @param page     number of page (first page has index 0), if null, default value is 0
     * @param pageSize number of items in one page,<br />
     *                 if null default value is defined by {@code PAGINATION_DEFAULT_PAGE_SIZE} env,<br />
     *                 max allowed size is defined by {@code PAGINATION_MAX_PAGE_SIZE} env
     * @param database database object
     * @return database language query with pagination, page number and page size
     * @throws BadRequestException pageSize value is greater than maximum allowed value
     */
    public static QueryService.PaginatedQuery setPaginationInSqlQuery(
            String query,
            Integer page,
            Integer pageSize,
            Database database) throws BadRequestException {

        // defaults
        if (page == null) {
            page = 0;
        }
        if (page < 0) {
            String error = "Page number cannot be negative, page=" + page;
            log.error(error);
            throw new BadRequestException(error);
        }
        if (pageSize == null) {
            pageSize = Settings.getDefaultPageSizeStatic();
        }
        if (pageSize > Settings.getMaxPageSizeStatic()) {
            String error = "Page size is greater than maximum allowed value=" + Settings.getMaxPageSizeStatic();
            log.error(error);
            throw new BadRequestException(error);
        }

        query = query.trim();

        query = switch (database.getEngine()) {
            case POSTGRES, MYSQL -> "SELECT * FROM (%s) AS query LIMIT %d OFFSET %d;"
                    .formatted(trimAndRemoveTrailingSemicolon(query), pageSize, page * pageSize);
        };

        return new QueryService.PaginatedQuery(query, page, pageSize);
    }

    // package private for testing
    static String trimAndRemoveTrailingSemicolon(String query) {
        query = query.trim();

        if (query.isEmpty()) {
            return query;
        }

        // removes trailing semicolon if it is present
        return query.charAt(query.length() - 1) == ';'
                ? query.substring(0, query.length() - 1).trim()
                : query;
    }
}
