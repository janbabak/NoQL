package com.janbabak.noqlbackend.service.query;

import com.janbabak.noqlbackend.dao.ResultSetWrapper;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.database.BaseDatabaseService;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;

import java.sql.SQLException;

@Slf4j
@UtilityClass
public class QueryUtils {

    public record PaginatedQuery(String query, Integer page, Integer pageSize) {
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
    public static PaginatedQuery setPaginationInSqlQuery(
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

        return new PaginatedQuery(query, page, pageSize);
    }

    public static String trimAndRemoveTrailingSemicolon(String query) {
        query = query.trim();

        if (query.isEmpty()) {
            return query;
        }

        // removes trailing semicolon if it is present
        return query.charAt(query.length() - 1) == ';'
                ? query.substring(0, query.length() - 1).trim()
                : query;
    }


    /**
     * Get total number of rows that SQL select query returns.
     *
     * @param selectQuery     select statement
     * @param database        database to query
     * @param databaseService service that can handle the query
     * @return total number of rows
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    @SuppressWarnings("all") // default branch unnecessary
    public static Long getTotalCount(String selectQuery, Database database, BaseDatabaseService databaseService)
            throws DatabaseConnectionException, DatabaseExecutionException, BadRequestException {

        return switch (database.getEngine()) {
            case POSTGRES, MYSQL -> getTotalCountSql(selectQuery, databaseService);
            default -> throw new BadRequestException(
                    "Getting total count not supported for database engine: " + database.getEngine());
        };
    }

    public static Long getTotalCountSql(String selectQuery, BaseDatabaseService databaseService)
            throws DatabaseConnectionException, DatabaseExecutionException {

        selectQuery = trimAndRemoveTrailingSemicolon(selectQuery);
        String selectCountQuery = "SELECT COUNT(*) AS count from (%s) AS all_results;".formatted(selectQuery);

        try (ResultSetWrapper result = databaseService.executeQuery(selectCountQuery)) {
            return result.resultSet().next() ? result.resultSet().getLong(1) : null;
        } catch (SQLException e) {
            throw new DatabaseExecutionException("Cannot parse total count value from query");
        }
    }
}
