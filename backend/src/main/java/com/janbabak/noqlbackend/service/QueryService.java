package com.janbabak.noqlbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.janbabak.noqlbackend.dao.ResultSetWrapper;
import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.*;
import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.chat.*;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.database.DatabaseStructure;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.*;
import com.janbabak.noqlbackend.service.api.LlmApiServiceFactory;
import com.janbabak.noqlbackend.service.api.QueryApi;
import com.janbabak.noqlbackend.service.chat.ChatQueryWithResponseService;
import com.janbabak.noqlbackend.service.chat.ChatService;
import com.janbabak.noqlbackend.service.database.BaseDatabaseService;
import com.janbabak.noqlbackend.service.database.DatabaseServiceFactory;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import com.janbabak.noqlbackend.service.user.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.DATABASE;
import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.MESSAGE;
import static com.janbabak.noqlbackend.service.utils.JsonUtils.createFromJson;

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

    private final LlmApiServiceFactory llmApiServiceFactory;
    private final Settings settings;
    private final ChatService chatService;
    private final PlotService plotService;
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final ChatQueryWithResponseService chatQueryWithResponseService;
    private final ChatRepository chatRepository;
    private final DatabaseRepository databaseRepository;
    private final ChatQueryWithResponseRepository chatQueryWithResponseRepository;

    /**
     * Create system query that commands the LLM with instructions. Use placeholders for connection to the database
     * that will be replaced latter by the actual values for security reasons.
     *
     * @param dbStructure structure of the database (in form of create script)
     * @param database    database
     * @return system query
     */
    @SuppressWarnings("all")
    public static String createSystemQuery(String dbStructure, Database database) {
        return new StringBuilder(
                """
                        You are an assistant that helps users visualise data. You have two functions. The first function
                        is translation of natural language queries into a database language. The second function is
                        visualising data. If the user wants to show or display or find or retrieve some data, translate
                        it into""")
                .append(database.isSQL() ? " an SQL query" : " an query language query")
                .append(" for the ")
                .append(database.getEngine().toString().toLowerCase(Locale.ROOT))
                .append("""
                         database. Generate this query nicely formatted with line breaks.
                        I will use this query for displaying the data in form of table. If the user wants to plot,
                        chart or visualize the data, create a Python script that will select the data and visualise them
                        in a chart. Save the generated chart into a file called""")
                .append(" ").append(PlotService.PLOT_DIRECTORY_DOCKER_PATH).append("/")
                .append(PLOT_FILE_NAME_PLACEHOLDER)
                .append("""
                         and don't show it.
                        To connect to the database use host='""").append(HOST_PLACEHOLDER)
                .append("', port=").append(PORT_PLACEHOLDER)
                .append(" , user='").append(USER_PLACEHOLDER)
                .append("', password='").append(PASSWORD_PLACEHOLDER)
                .append("', database='").append(DATABASE_PLACEHOLDER).append("'.\n\n")
                .append("""                       
                        Your response must be in JSON format
                        { databaseQuery: string, generatePlot: boolean, pythonCode: string }.
                        
                        The database structure looks like this:""")
                .append(dbStructure)
                .toString();
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
     * Sometimes the model does not return just the query itself, but puts it into a markdown and add some text. <br />
     * Extract the executable query from the response that can look like this: <br />
     * {@code Use the following command to retrieve all users. ```json\n{ query: "...", ...}```} <br />
     *
     * @param response model's response
     * @return executable query
     */
    public String extractQueryFromMarkdownInResponse(@NotNull String response) {

        final String jsonMarkdownPatterStart = "```json";
        final String markdownPatterEnd = "```";
        final String genericMarkdownPatterStart = "```";
        String markdownPatterStart = jsonMarkdownPatterStart;

        int markdownIndex = response.indexOf(markdownPatterStart);

        // markdown not detected
        if (markdownIndex == -1) {
            markdownPatterStart = genericMarkdownPatterStart;
            markdownIndex = response.indexOf(markdownPatterStart);
            if (markdownIndex == -1) {
                return response; // unable to parse
            }
        }

        if (response.length() < markdownIndex + markdownPatterStart.length()) {
            return response; // unable to parse
        }
        String responseAfterMarkdownStart = response.substring(
                markdownIndex + markdownPatterStart.length());

        markdownIndex = responseAfterMarkdownStart.indexOf(markdownPatterEnd);
        if (markdownIndex == -1) {
            return response; // unable to parse
        }

        String extractedJson = responseAfterMarkdownStart.substring(0, markdownIndex).trim();

        log.info("Extracted query={} from response={}", extractedJson, response);

        return extractedJson;
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
    private static Long getTotalCount(String selectQuery, BaseDatabaseService databaseService)
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

        BaseDatabaseService databaseService = DatabaseServiceFactory.getDatabaseService(database);
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

        return retrieveDataFromMessage(message, message.getChat().getDatabase(), page, pageSize);
    }

    /**
     * Retrieve data from the message.
     *
     * @param message  message
     * @param database database
     * @param page     page number (starting by 0)
     * @param pageSize number of items per page
     * @return retrieved data
     */
    public static RetrievedData retrieveDataFromMessage(
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

        BaseDatabaseService databaseService = DatabaseServiceFactory.getDatabaseService(database);
        QueryService.PaginatedQuery paginatedQuery;
        try {
            paginatedQuery = setPaginationInSqlQuery(LLMResponse.databaseQuery(), page, pageSize, database);
        } catch (BadRequestException e) {
            return null; // should not happen
        }
        try (ResultSetWrapper result = databaseService.executeQuery(paginatedQuery.query)) {
            Long totalCount = getTotalCount(LLMResponse.databaseQuery(), databaseService);
            return new RetrievedData(result.resultSet(), paginatedQuery.page, paginatedQuery.pageSize, totalCount);
        } catch (DatabaseExecutionException | SQLException | DatabaseConnectionException e) {
            return null; // should not happen since not executable responses aren't saved
        }
    }

    /**
     * Retrieve data requested by the user in form of table or plot or both.
     *
     * @param queryRequest    query request
     * @param chatId          chat identifier
     * @param llmResponseJson LLM unparsed response
     * @param databaseService specific database service
     * @param database        database
     * @param pageSize        number of rows per page
     * @return query response with retrieved data and now plot
     * @throws BadRequestException          pageSize value is greater than maximum allowed value
     * @throws DatabaseConnectionException  cannot establish database connection
     * @throws DatabaseExecutionException   query execution failed - syntax error, ...
     * @throws SQLException                 error when retrieving data
     * @throws EntityNotFoundException      chat not found
     * @throws PlotScriptExecutionException plot execution failed - bad syntax, IO error, ...
     * @throws JsonProcessingException      LLM response cannot be parsed from JSON - syntax error
     */
    private ChatResponse showResultTableAndGeneratePlot(
            QueryRequest queryRequest,
            UUID chatId,
            String llmResponseJson,
            BaseDatabaseService databaseService,
            Database database,
            Integer pageSize) throws BadRequestException, DatabaseConnectionException,
            DatabaseExecutionException, SQLException, EntityNotFoundException, PlotScriptExecutionException,
            JsonProcessingException {

        LLMResponse llmResponse = createFromJson(llmResponseJson, LLMResponse.class);

        ChatQueryWithResponse chatQueryWithResponse = chatService.addMessageToChat(chatId,
                new CreateChatQueryWithResponseRequest(queryRequest.getQuery(), llmResponseJson));

        String plotFileName = null;

        if (llmResponse.generatePlot()) {
            log.info("Generate plot");

            plotFileName = plotService.generatePlot(
                    llmResponse.pythonCode(), database, chatId, chatQueryWithResponse.getId());
        }

        RetrievedData retrievedData = null;
        Long totalCount;

        if (llmResponse.databaseQuery() != null && !llmResponse.databaseQuery().isEmpty()) {

            PaginatedQuery paginatedQuery = setPaginationInSqlQuery(
                    llmResponse.databaseQuery(), 0, pageSize, database);

            try (ResultSetWrapper result = databaseService.executeQuery(paginatedQuery.query)) {
                totalCount = getTotalCount(llmResponse.databaseQuery(), databaseService);

                retrievedData = new RetrievedData(
                        result.resultSet(),
                        paginatedQuery.page,
                        paginatedQuery.pageSize,
                        totalCount);
            }
        }

        return new ChatResponse(retrievedData, chatQueryWithResponse, llmResponse, plotFileName);
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
     * @throws LLMException                large language model failure
     */
    public ChatResponse queryChat(UUID databaseId, UUID chatId, QueryRequest queryRequest, Integer pageSize)
            throws EntityNotFoundException, DatabaseConnectionException, LLMException,
            DatabaseExecutionException, BadRequestException {

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

        BaseDatabaseService specificDatabaseService = DatabaseServiceFactory.getDatabaseService(database);
        DatabaseStructure databaseStructure = specificDatabaseService.retrieveSchema();
        String systemQuery = createSystemQuery(databaseStructure.generateCreateScript(), database);
        List<String> errors = new ArrayList<>();
        List<ChatQueryWithResponse> chatHistory = chatQueryWithResponseService.getMessagesFromChat(chatId);
        String llmResponseJson;

        QueryApi queryApi = llmApiServiceFactory.getQueryApiService(queryRequest.getModel());

        for (int attempt = 1; attempt <= settings.translationRetries; attempt++) {
            llmResponseJson = queryApi.queryModel(chatHistory, queryRequest, systemQuery, errors);
            if (llmResponseJson == null) {
                errors.add("Response is null");
                continue;
            } else {
                llmResponseJson = extractQueryFromMarkdownInResponse(llmResponseJson);
            }

            try {
                return showResultTableAndGeneratePlot(
                        queryRequest, chatId, llmResponseJson, specificDatabaseService, database, pageSize);
            } catch (JsonProcessingException e) {
                errors.add("Cannot parse LLM response JSON");
                log.error("Cannot parse response JSON: {}", llmResponseJson);
            } catch (SQLException e) {
                errors.add("Error occurred when execution your query: " + e.getMessage());
            } catch (PlotScriptExecutionException e) {
                errors.add(e.getMessage());
            } catch (DatabaseExecutionException e) {
                errors.add("Generated query execution failed");
            }
        }

        // last try failed - return message that is not persisted
        String lastError = !errors.isEmpty() ? errors.get(errors.size() - 1) : null;
        return ChatResponse.failedResponse(lastError, queryRequest.getQuery());
    }
}
