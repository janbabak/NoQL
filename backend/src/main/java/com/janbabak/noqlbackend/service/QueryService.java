package com.janbabak.noqlbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.*;
import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.chat.LLMResponse;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.database.DatabaseStructure;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.chat.ChatQueryWithResponseDto;
import com.janbabak.noqlbackend.model.query.QueryResponse;
import com.janbabak.noqlbackend.model.query.QueryResponse.RetrievedData;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.service.api.LlmApiServiceFactory;
import com.janbabak.noqlbackend.service.api.QueryApi;
import com.janbabak.noqlbackend.service.chat.ChatQueryWithResponseService;
import com.janbabak.noqlbackend.service.chat.ChatService;
import com.janbabak.noqlbackend.service.database.BaseDatabaseService;
import com.janbabak.noqlbackend.service.database.DatabaseServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.DATABASE;
import static com.janbabak.noqlbackend.model.query.QueryResponse.ColumnTypes;
import static com.janbabak.noqlbackend.model.chat.ChatQueryWithResponseDto.LLMResult;
import static com.janbabak.noqlbackend.service.utils.JsonUtils.createFromJson;
import static java.sql.Types.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryService {
    private final DatabaseRepository databaseRepository;
    private final ChatQueryWithResponseRepository chatQueryWithResponseRepository;
    private final Settings settings;
    private final ChatService chatService;
    private final ChatQueryWithResponseService chatQueryWithResponseService;
    private final PlotService plotService;

    /**
     * Create system query that commands the LLM with instructions
     *
     * @param dbStructure structure of the database (in form of create script)
     * @param database    database
     * @param chatId      chat identifier
     * @return system query
     */
    @SuppressWarnings("all")
    public static String createSystemQuery(String dbStructure, Database database, UUID chatId) {
        // TODO: securly insert credentials from coresponding database
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
                         database. I will use this query for displaying the data in form of table. If the user wants to
                        plot, chart or visualize the data, create a Python script that will select the data and
                        visualise them in a chart. Save the generated chart into a file called""")
                .append(" " + PlotService.plotsDirPath.get() + "/" + chatId + PlotService.PLOT_IMAGE_FILE_EXTENSION)
                .append("""
                         and don't show it.
                        To connect to the database use host='localhost',
                        port=5432, user='user', password='password', database='database'.
                                                
                        Your response must be in JSON format
                        { databaseQuery: string, generatePlot: boolean, pythonCode: string }.
                                         
                        The database structure looks like this:""")
                .append(dbStructure)
                .toString();
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
     * @return database language query with pagination
     * @throws BadRequestException pageSize value is greater than maximum allowed value
     */
    public String setPaginationInSqlQuery(String query, Integer page, Integer pageSize, Database database)
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
            pageSize = settings.getDefaultPageSize();
        }
        if (pageSize > settings.getMaxPageSize()) {
            String error = "Page size is greater than maximum allowed value=" + settings.getMaxPageSize();
            log.error(error);
            throw new BadRequestException(error);
        }

        query = query.trim();

        return switch (database.getEngine()) {
            case POSTGRES, MYSQL -> "SELECT * FROM (%s) LIMIT %d OFFSET %d;"
                    .formatted(trimAndRemoveTrailingSemicolon(query), pageSize, page * pageSize);
        };
    }

    // package private for testing
    String trimAndRemoveTrailingSemicolon(String query) {
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
    public String extractQueryFromMarkdownInResponse(String response) {
        int markdownIndex = response.indexOf("```");

        // markdown not detected
        if (markdownIndex == -1) {
            return response;
        }

        String responseAfterMarkdownStart = response.substring(markdownIndex);
        // I cannot cut only the ``` because it usually looks like ```json\n or something like that
        int newLineIndex = responseAfterMarkdownStart.indexOf("\n");
        if (newLineIndex == -1) {
            newLineIndex = responseAfterMarkdownStart.indexOf(" ");
        }
        if (newLineIndex == -1 || newLineIndex + 1 > responseAfterMarkdownStart.length() - 1) {
            return response; // unable to parse
        }
        String responseAfterNewLine = responseAfterMarkdownStart.substring(newLineIndex + 1);
        markdownIndex = responseAfterNewLine.indexOf("```");
        if (markdownIndex == -1) {
            return response; // unable to parse
        }

        String extractedQuery = responseAfterNewLine.substring(0, markdownIndex).trim();

        log.info("Extracted query={} from response={}", extractedQuery, response);

        return extractedQuery;
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
     * Heuristic that decides whether a column is categorical or not. Categorical column is a column where
     * number of unique values < number of all values / 3
     *
     * @param columnName      examined column
     * @param selectQuery     select to retrieve entire result without pagination
     * @param queryResultSet  result of executed selectQuery
     * @param databaseService service that can handle the query
     * @return true if categorical, otherwise false
     */
    private boolean isCategorical(
            String columnName,
            String selectQuery,
            ResultSet queryResultSet,
            BaseDatabaseService databaseService)
            throws DatabaseConnectionException, DatabaseExecutionException, SQLException {
        selectQuery = trimAndRemoveTrailingSemicolon(selectQuery);

        String selectNumberOfValues = "SELECT COUNT(DISTINCT %s) AS distinctCount, COUNT(%s) AS count FROM (%s);"
                .formatted(columnName, columnName, selectQuery);

        ResultSet resultSet = databaseService.executeQuery(selectNumberOfValues);

        long totalCount;
        long distinctCount;
        try {
            if (!resultSet.next()) {
                return false; // shouldn't happen
            }
            distinctCount = resultSet.getLong(1);
            totalCount = resultSet.getLong(2);
        } catch (SQLException e) {
            throw new DatabaseExecutionException("Cannot parse counts values from query");
        }

        return switch (getColumnDataType(columnName, queryResultSet)) {
            // categorical data are usually numbers, strings or booleans
            case BIT, TINYINT, SMALLINT, INTEGER, BIGINT, NUMERIC, CHAR, VARCHAR,
                    LONGVARCHAR, BOOLEAN, NCHAR, NVARCHAR, LONGNVARCHAR -> distinctCount * 3 < totalCount;
            default -> false;
        };
    }

    /**
     * Decides whether a column is numerical or not.
     *
     * @param columnName     examined column
     * @param queryResultSet result of executed query
     * @return true if is numerical, false otherwise
     * @throws SQLException ... TODO
     */
    private boolean isNumerical(String columnName, ResultSet queryResultSet) throws SQLException {
        return switch (getColumnDataType(columnName, queryResultSet)) {
            case BIT, TINYINT, SMALLINT, INTEGER, BIGINT, NUMERIC -> true;
            default -> false;
        };
    }

    /**
     * Decides whether a column is numerical or not.
     *
     * @param columnName     examined column
     * @param queryResultSet result of executed query
     * @return true if is timestamp, false otherwise
     * @throws SQLException ... TODO
     */
    private boolean isTimestamp(String columnName, ResultSet queryResultSet) throws SQLException {
        return switch (getColumnDataType(columnName, queryResultSet)) {
            case DATE, TIME, TIMESTAMP -> true;
            default -> false;
        };
    }

    /**
     * Get data type of column.
     *
     * @param columnName name of investigated column
     * @param resultSet  result set that contains columName column
     * @return int value that represents the datatype, 0 if column not found
     * @throws SQLException ... TODO
     */
    private int getColumnDataType(String columnName, ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsmd = resultSet.getMetaData();

        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            if (Objects.equals(columnName, rsmd.getColumnName(i))) {
                return rsmd.getColumnType(i);
            }
        }
        return 0;
    }

    // TODO: remove or use
    /**
     * Classify columns into column types
     *
     * @param resultSet               query result
     * @param query                   generated query without pagination
     * @param retrievedData           result object
     * @param specificDatabaseService specific database service capable of executing query
     * @return column types
     * @throws SQLException                TODO
     * @throws DatabaseConnectionException connection failure
     * @throws DatabaseExecutionException  execution fail
     */
    @SuppressWarnings("unused")
    private ColumnTypes getColumnTypes(
            ResultSet resultSet,
            String query,
            RetrievedData retrievedData,
            BaseDatabaseService specificDatabaseService
    ) throws SQLException, DatabaseConnectionException, DatabaseExecutionException {
        ColumnTypes columnTypes = new ColumnTypes();
        for (String columnName : retrievedData.getColumnNames()) {
            if (isCategorical(columnName, query, resultSet, specificDatabaseService)) {
                columnTypes.addCategorical(columnName);
            }
            if (isNumerical(columnName, resultSet)) {
                columnTypes.addNumeric(columnName);
            }
            if (isTimestamp(columnName, resultSet)) {
                columnTypes.addTimestamp(columnName);
            }
        }
        return columnTypes;
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
    public QueryResponse executeQueryLanguageSelectQuery(
            UUID databaseId,
            String query,
            Integer page,
            Integer pageSize
    ) throws EntityNotFoundException, DatabaseConnectionException, BadRequestException {

        log.info("Execute query language query: query={}, database_id={}.", query, databaseId);

        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        BaseDatabaseService databaseService = DatabaseServiceFactory.getDatabaseService(database);
        String paginatedQuery = setPaginationInSqlQuery(query, page, pageSize, database);

        try {
            ResultSet resultSet = databaseService.executeQuery(paginatedQuery);
            return QueryResponse.successfulResponse(
                    new RetrievedData(resultSet), null, getTotalCount(query, databaseService));
        } catch (DatabaseExecutionException | SQLException e) {
            return QueryResponse.failedResponse(null, e.getMessage());
        }
    }

    /**
     * Load result of response of last message from chat. Used when user opens an old chat.
     *
     * @param databaseId identifier of the database to which the selected chat belongs
     * @param chatId     chat to load identifier
     * @param page       page number (first pages has is 0)
     * @param pageSize   number of items per page
     * @return query response
     * @throws EntityNotFoundException     database or chat not found
     * @throws BadRequestException         pageSize value is greater than maximum allowed value
     * @throws DatabaseConnectionException cannot establish connection with database
     */
    public QueryResponse loadChatResult(UUID databaseId, UUID chatId, Integer page, Integer pageSize)
            throws EntityNotFoundException, BadRequestException, DatabaseConnectionException {
        log.info("Reload chat result, chatId={}", chatId);

        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        ChatQueryWithResponse latestMessage =
                chatQueryWithResponseRepository.findLatestMessageFromChat(chatId).orElse(null);
        if (latestMessage == null) {
            return null;
        }

        LLMResponse LLMResponse;
        try {
            LLMResponse = createFromJson(latestMessage.getLlmResponse(), LLMResponse.class);
        } catch (JsonProcessingException e) {
            return null; // should not happen since values that cannot be parsed aren't saved
        }

        ChatQueryWithResponseDto chatQueryWithResponseDto =
                new ChatQueryWithResponseDto(latestMessage, new LLMResult(LLMResponse, chatId));

        // only plot without any select query to retrieve the data
        if (LLMResponse.getDatabaseQuery() == null) {
            return QueryResponse.successfulResponse(null, chatQueryWithResponseDto, null);
        }

        BaseDatabaseService databaseService = DatabaseServiceFactory.getDatabaseService(database);
        String paginatedQuery = setPaginationInSqlQuery(LLMResponse.getDatabaseQuery(), page, pageSize, database);

        try {
            ResultSet resultSet = databaseService.executeQuery(paginatedQuery);
            RetrievedData retrievedData = new RetrievedData(resultSet);
            Long totalCount = getTotalCount(LLMResponse.getDatabaseQuery(), databaseService);

            return QueryResponse.successfulResponse(retrievedData, chatQueryWithResponseDto, totalCount);
        } catch (DatabaseExecutionException | SQLException e) {
            // should not happen since not executable responses aren't saved
            return QueryResponse.failedResponse(chatQueryWithResponseDto, e.getMessage());
        }
    }

    /**
     * Retrieve data requested by the user in form of table or plot or both.
     *
     * @param queryRequest    query request
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
    private QueryResponse showResultTableAndGeneratePlot(
            QueryRequest queryRequest,
            String llmResponseJson,
            BaseDatabaseService databaseService,
            Database database,
            Integer pageSize) throws BadRequestException, DatabaseConnectionException, DatabaseExecutionException,
            SQLException, EntityNotFoundException, PlotScriptExecutionException, JsonProcessingException {

        LLMResponse llmResponse = createFromJson(llmResponseJson, LLMResponse.class);

        if (llmResponse.getGeneratePlot()) {
            log.info("Generate plot");
            plotService.generatePlot(llmResponse.getPythonCode());
        }

        RetrievedData retrievedData = null;
        Long totalCount = null;
        if (llmResponse.getDatabaseQuery() != null || !llmResponse.getGeneratePlot()) {
            String paginatedQuery = setPaginationInSqlQuery(llmResponse.getDatabaseQuery(), 0, pageSize, database);
            ResultSet resultSet = databaseService.executeQuery(paginatedQuery);
            retrievedData = new RetrievedData(resultSet);
            totalCount = getTotalCount(llmResponse.getDatabaseQuery(), databaseService);
        }

        ChatQueryWithResponse chatQueryWithResponse = chatService.addMessageToChat(
                queryRequest.getChatId(),
                new CreateChatQueryWithResponseRequest(queryRequest.getQuery(), llmResponseJson));

        ChatQueryWithResponseDto chatQueryWithResponseDto = new ChatQueryWithResponseDto(
                chatQueryWithResponse, new LLMResult(llmResponse, queryRequest.getChatId()));

        return QueryResponse.successfulResponse(retrievedData, chatQueryWithResponseDto, totalCount);
    }

    /**
     * Execute natural language select query from the chat. The query is translated to specific dialect via LLM
     * and executed. Select query is read only.
     *
     * @param databaseId   database id
     * @param queryRequest query
     * @param pageSize     number of items per page
     * @return result that contains data in form of table that is automatically paginated starting by page 0 or plot
     * or both.
     * @throws EntityNotFoundException     queried database not found.
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  retrieving database schema failure
     * @throws LLMException                large language model failure
     */
    public QueryResponse executeChat(UUID databaseId, QueryRequest queryRequest, Integer pageSize)
            throws EntityNotFoundException, DatabaseConnectionException, LLMException,
            DatabaseExecutionException, BadRequestException {

        log.info("Execute chat, database_id={}", databaseId);

        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        BaseDatabaseService specificDatabaseService = DatabaseServiceFactory.getDatabaseService(database);
        DatabaseStructure databaseStructure = specificDatabaseService.retrieveSchema();
        String systemQuery = createSystemQuery(
                databaseStructure.generateCreateScript(), database, queryRequest.getChatId());
        List<String> errors = new ArrayList<>();
        List<ChatQueryWithResponse> chatHistory =
                chatQueryWithResponseService.getMessagesFromChat(queryRequest.getChatId());
        String llmResponseJson = "";

        QueryApi queryApi = LlmApiServiceFactory.getQueryApiService(queryRequest.getModel());

        for (int attempt = 1; attempt <= settings.translationRetries; attempt++) {
            llmResponseJson = queryApi.queryModel(chatHistory, queryRequest, systemQuery, errors);
            llmResponseJson = extractQueryFromMarkdownInResponse(llmResponseJson);

            try {
                return showResultTableAndGeneratePlot(
                        queryRequest, llmResponseJson, specificDatabaseService, database, pageSize);
            } catch (JsonProcessingException e) {
                errors.add("Cannot parse response JSON - bad syntax.");
            } catch (SQLException e) {
                errors.add("Error occurred when execution your query: " + e.getMessage());
            } catch (PlotScriptExecutionException e) {
                errors.add(e.getMessage());
            } catch (DatabaseExecutionException e) {
                errors.add("Generated query execution failed");
            }
        }

        // last try failed - return message that is not persisted
        ChatQueryWithResponse message = new ChatQueryWithResponse();
        message.setNlQuery(queryRequest.getQuery());
        message.setLlmResponse(llmResponseJson);

        String lastError = !errors.isEmpty() ? errors.get(errors.size() - 1) : null;
        return QueryResponse.failedResponse(new ChatQueryWithResponseDto(message, null), lastError);
    }
}
