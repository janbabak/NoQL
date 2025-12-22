package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.ResultSetWrapper;
import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.*;
import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.*;
import com.janbabak.noqlbackend.service.chat.ChatQueryWithResponseService;
import com.janbabak.noqlbackend.service.chat.ChatService;
import com.janbabak.noqlbackend.service.database.BaseDatabaseService;
import com.janbabak.noqlbackend.service.database.DatabaseServiceFactory;
import com.janbabak.noqlbackend.service.database.MessageDataDAO;
import com.janbabak.noqlbackend.service.langChain.QueryDatabaseLLMService;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import com.janbabak.noqlbackend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.DATABASE;
import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryService {
    public final static String PASSWORD_PLACEHOLDER = "dkl45349?405";
    public final static String USER_PLACEHOLDER = "admin4445900234";
    public final static String DATABASE_PLACEHOLDER = "database99889899";
    public final static String PORT_PLACEHOLDER = "1111111111";
    public final static String HOST_PLACEHOLDER = "localhost";
    public final static String PLOT_FILE_NAME_PLACEHOLDER = "noQlGeneratedPlot.png";
    /**
     * if host = {@code localhost} then it is replaced by this value, works on mac OS, not sure about other systems
     */
    public final static String DOCKER_LOCALHOST = "host.docker.internal";

    private final ChatService chatService;
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final ChatQueryWithResponseService chatQueryWithResponseService;
    private final QueryDatabaseLLMService llmService;
    private final ChatRepository chatRepository;
    private final DatabaseRepository databaseRepository;
    private final ChatQueryWithResponseRepository chatQueryWithResponseRepository;
    private final DatabaseServiceFactory databaseServiceFactory;
    private final MessageDataDAO messageDataDAO;

    /**
     * Create system query that commands the LLM with instructions. Use placeholders for connection to the database
     * that will be replaced latter by the actual values for security reasons.
     *
     * @param dbStructure structure of the database (in form of create script)
     * @param database    database
     * @return system query
     */
    @SuppressWarnings("all")
    public static String createExperimentalSystemQuery(String dbStructure, Database database) {
        return """
                You are an AI agent that helps users with data analysis and visualisation by translating their requests
                in natural language into database queries and Python scripts for data visualisation.
                
                You have the following tools at your disposal:
                
                The first function is 'executeQuery' that executes query in valid database query language. In
                this case ${IS_SQL} query for the ${DB_ENGINE} database. If users asks for data, you can generate a
                query based on their input in natural language, call this function with the query as parameter. The
                system will present retrieved data to users in form of table. Generate this query nicely formatted with
                line breaks so it can be presented to users with the data table.
                
                The second function is 'generatePlot' that creates a plot from data in the database. If users
                want to visualize data to see them in form of chart such as pie chart, bar chart, line chart,
                scatter plot, and others charts, you can generate a Python script that selects the data and visualize
                them in a chart using the matplotlib library. Save the generated chart into a file called
                ${PLOT_FILE_NAME} and don't show it. This Python script will be executed and the resulting
                plot will be presented to users, don't worry about the presentation. To connect to the database use
                host='${DB_HOST}', port=${DB_PORT}, user='${DB_USER}', password='${DB_PASSWORD}', database='${DB_NAME}'.
                
                Users may ask for just one of these two functions or both of them. If users ask for data, the first
                function should be called. If users ask for plot, the second function should be called. If user asks for
                data visualisation in form of chart and he can benefit from seeing the data in form of table, you can
                call both functions. If users want to see a chart but don't specify the type of chart, choose the most
                suitable chart type based on the data and context.
                
                Response with brief explanation of the results to help users understand them better.
                
                To help you generate better queries and plots, here is the structure of the database:
                ${DB_STRUCTURE}
                """
                .replace("${IS_SQL}", database.isSQL() ? "SQL" : "non-SQL")
                .replace("${DB_ENGINE}", database.getEngine().toString().toLowerCase(Locale.ROOT))
                .replace("${PLOT_FILE_NAME}",
                        PlotService.PLOT_DIRECTORY_DOCKER_PATH + "/" + PLOT_FILE_NAME_PLACEHOLDER)
                .replace("${DB_HOST}", HOST_PLACEHOLDER)
                .replace("${DB_PORT}", PORT_PLACEHOLDER)
                .replace("${DB_USER}", USER_PLACEHOLDER)
                .replace("${DB_PASSWORD}", PASSWORD_PLACEHOLDER)
                .replace("${DB_NAME}", DATABASE_PLACEHOLDER)
                .replace("${DB_STRUCTURE}", dbStructure);
    }

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
    public static PaginatedQuery setPaginationInSqlQuery(String query, Integer page, Integer pageSize, Database database)
            throws BadRequestException {
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

    /**
     * Get total number of rows that SQL select query returns.
     *
     * @param selectQuery     select statement
     * @param databaseService service that can handle the query
     * @return total number of rows
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    public static Long getTotalCount(String selectQuery, BaseDatabaseService databaseService)
            throws DatabaseConnectionException, DatabaseExecutionException {

        selectQuery = trimAndRemoveTrailingSemicolon(selectQuery);
        String selectCountQuery = "SELECT COUNT(*) AS count from (%s) AS all_results;".formatted(selectQuery);
        try (ResultSetWrapper result = databaseService.executeQuery(selectCountQuery)) {
            return result.resultSet().next() ? result.resultSet().getLong(1) : null;
        } catch (SQLException e) {
            throw new DatabaseExecutionException("Cannot parse total count value from query");
        }
    }

    /**
     * Execute query language select query.
     * Select query is read only, and it returns a result that is automatically paginated.
     *
     * @param databaseId database identifier
     * @param query      in database query language
     * @param page       page number (fist page starts by 0), if null, default value is 0
     * @param pageSize   number of items in one page,<br />
     *                   default value is defined by {@code PAGINATION_DEFAULT_PAGE_SIZE} env,<br />
     *                   max allowed size is defined by {@code PAGINATION_MAX_PAGE_SIZE} env
     * @return query result
     * @throws EntityNotFoundException     queried database not found.
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws BadRequestException         pageSize value is greater than maximum allowed value
     */
    public ConsoleResponse executeQueryLanguageSelectQuery(
            UUID databaseId,
            String query,
            Integer page,
            Integer pageSize
    ) throws EntityNotFoundException, DatabaseConnectionException, BadRequestException {

        log.info("Execute query language query: query={}, database_id={}.", query, databaseId);

        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(database.getUserId());

        BaseDatabaseService databaseService = databaseServiceFactory.getDatabaseService(database);
        String paginatedQuery = setPaginationInSqlQuery(query, page, pageSize, database).query;

        try (ResultSetWrapper result = databaseService.executeQuery(paginatedQuery)) {
            return ConsoleResponse.builder()
                    .data(new RetrievedData(
                            result.resultSet(),
                            page,
                            pageSize,
                            getTotalCount(query, databaseService)))
                    .dbQuery(query)
                    .build();
        } catch (DatabaseExecutionException | SQLException e) {
            return ConsoleResponse.failedResponse(e.getMessage());
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

        return messageDataDAO.retrieveDataFromMessage(message, message.getChat().getDatabase(), page, pageSize);
    }


    /**
     * Query natural chat with natural language query. The query is translated to specific dialect via LLM
     * and executed. Select query is read only.
     *
     * @param databaseId   database id
     * @param chatId       chat id
     * @param queryRequest query
     * @param pageSize     number of items per page
     * @return result that contains data in form of table that is automatically paginated starting by page 0 or plot
     * or both.
     * @throws EntityNotFoundException     queried database not found.
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  retrieving database schema failure
     */
    public ChatResponse queryChat(UUID databaseId, UUID chatId, QueryRequest queryRequest, Integer pageSize)
            throws EntityNotFoundException, DatabaseConnectionException, DatabaseExecutionException {

        log.info("Execute chat, database_id={}", databaseId);

        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(database.getUserId());

        chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException(EntityNotFoundException.Entity.CHAT, chatId));

        if (userService.decrementQueryLimit(database.getUserId()) <= 0) {
            log.info("Query limit exceeded");
            return ChatResponse.failedResponse("Query limit exceeded", queryRequest.getQuery());
        }

        List<ChatQueryWithResponse> chatHistory = chatQueryWithResponseService.getMessagesFromChat(chatId);
        ChatQueryWithResponse chatQueryWithResponse = chatService.addEmptyMessageToChat(chatId);
        String plotFileName = PlotService.createFileName(chatId, chatQueryWithResponse.getId());

        // TODO: create request object
        QueryDatabaseLLMService.LLMServiceResult response = llmService.executeUserRequest(
                queryRequest.getQuery(),
                createExperimentalSystemQuery(
                        databaseServiceFactory.getDatabaseService(database).retrieveSchema().generateCreateScript(),
                        database),
                database,
                plotFileName,
                queryRequest.getModel(),
                pageSize,
                chatHistory);

         chatQueryWithResponse = chatQueryWithResponseService.updateEmptyMessage(
                 chatQueryWithResponse, queryRequest.getQuery(), response);

        String plotUrl = chatQueryWithResponse.getPlotScript() != null
                ? PlotService.createFileUrl(plotFileName)
                : null;

        return new ChatResponse(response.toolResult().getRetrievedData(), chatQueryWithResponse, plotUrl);
    }
}
