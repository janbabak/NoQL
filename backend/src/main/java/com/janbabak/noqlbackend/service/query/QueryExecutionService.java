package com.janbabak.noqlbackend.service.query;

import com.janbabak.noqlbackend.dao.ResultSetWrapper;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.query.RetrievedData;
import com.janbabak.noqlbackend.service.database.BaseDatabaseService;
import com.janbabak.noqlbackend.service.database.DatabaseServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

import static com.janbabak.noqlbackend.service.query.QueryUtils.getTotalCount;
import static com.janbabak.noqlbackend.service.query.QueryUtils.setPaginationInSqlQuery;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryExecutionService {

    private final DatabaseServiceFactory databaseServiceFactory;


    public RetrievedData executeQuery(String query, Database database, int page, int pageSize)
            throws BadRequestException, DatabaseConnectionException, DatabaseExecutionException, SQLException {

        validateQuery(query);

        QueryUtils.PaginatedQuery paginatedQuery = setPaginationInSqlQuery(query, page, pageSize, database);
        BaseDatabaseService databaseService = databaseServiceFactory.getDatabaseService(database);

        try (ResultSetWrapper result = databaseService.executeQuery(paginatedQuery.query())) {
            Long totalCount = getTotalCount(query, database, databaseService);
            return new RetrievedData(result.resultSet(), paginatedQuery.page(), paginatedQuery.pageSize(), totalCount);
        }
    }

    private void validateQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be null or blank");
        }
    }
}
