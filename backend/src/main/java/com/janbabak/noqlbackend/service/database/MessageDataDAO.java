package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.ResultSetWrapper;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.query.RetrievedData;
import com.janbabak.noqlbackend.service.query.QueryUtils;
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
public class MessageDataDAO {

    private final DatabaseServiceFactory databaseServiceFactory;

    /**
     * Retrieve data from the message.
     *
     * @param message  message
     * @param database database
     * @param page     page number (starting by 0)
     * @param pageSize number of items per page
     * @return retrieved data
     */
    public RetrievedData retrieveDataFromMessage(
            ChatQueryWithResponse message,
            Database database,
            Integer page,
            Integer pageSize) {

        if (!message.dbQuerySuccessfullyExecuted()) {
            return null;
        }

        BaseDatabaseService databaseService = databaseServiceFactory.getDatabaseService(database);
        QueryUtils.PaginatedQuery paginatedQuery;
        try {
            paginatedQuery = setPaginationInSqlQuery(message.getDbQuery(), page, pageSize, database);
        } catch (BadRequestException e) {
            log.error("Failed to set pagination in SQL query: {}", e.getMessage());
            return null;
        }
        try (ResultSetWrapper result = databaseService.executeQuery(paginatedQuery.query())) {
            Long totalCount = getTotalCount(message.getDbQuery(), database, databaseService);
            return new RetrievedData(result.resultSet(), paginatedQuery.page(), paginatedQuery.pageSize(), totalCount);
        } catch (DatabaseExecutionException | SQLException | DatabaseConnectionException | BadRequestException e) {
            log.error("Failed to retrieve data from message {}: {}", message.getId(), e.getMessage());
            return null;
        }
    }
}
