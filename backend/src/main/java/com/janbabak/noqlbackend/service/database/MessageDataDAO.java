package com.janbabak.noqlbackend.service.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.janbabak.noqlbackend.dao.ResultSetWrapper;
import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.LLMResponse;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.query.RetrievedData;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.MESSAGE;
import static com.janbabak.noqlbackend.service.utils.JsonUtils.createFromJson;

@Service
@RequiredArgsConstructor
public class MessageDataDAO {

    private final DatabaseServiceFactory databaseServiceFactory;
    private final ChatQueryWithResponseRepository chatQueryWithResponseRepository;
    private final AuthenticationService authenticationService;

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

        LLMResponse LLMResponse;
        try {
            LLMResponse = createFromJson(message.getLlmResponse(), LLMResponse.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return null; // should not happen since values that cannot be parsed aren't saved
        }

        // only plot without any select query to retrieve the data
        if (LLMResponse.databaseQuery() == null || LLMResponse.databaseQuery().isEmpty()) {
            return null;
        }

        BaseDatabaseService databaseService = databaseServiceFactory.getDatabaseService(database);
        databaseService.setDatabaseDaoMetadata(database);
        QueryService.PaginatedQuery paginatedQuery;
        try {
            paginatedQuery = QueryService.setPaginationInSqlQuery(
                    LLMResponse.databaseQuery(), page, pageSize, database);
        } catch (BadRequestException e) {
            return null; // should not happen
        }
        try (ResultSetWrapper result = databaseService.executeQuery(paginatedQuery.query())) {
            Long totalCount = QueryService.getTotalCount(LLMResponse.databaseQuery(), databaseService);
            return new RetrievedData(result.resultSet(), paginatedQuery.page(), paginatedQuery.pageSize(), totalCount);
        } catch (DatabaseExecutionException | SQLException | DatabaseConnectionException e) {
            return null; // should not happen since not executable responses aren't saved
        }
    }

    /**
     * Load result of response of last message from chat. Used when user opens an old chat.
     *
     * @param messageId identifier of the message to load
     * @param page      page number (first pages has is 0)
     * @param pageSize  number of items per page
     * @return query response
     * @throws EntityNotFoundException database or chat not found
     */
    public RetrievedData getDataByMessageId(
            UUID messageId,
            Integer page,
            Integer pageSize) throws EntityNotFoundException {

        ChatQueryWithResponse message = chatQueryWithResponseRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException(MESSAGE, messageId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(message.getChat().getDatabase().getUserId());

        return retrieveDataFromMessage(message, message.getChat().getDatabase(), page, pageSize);
    }
}
