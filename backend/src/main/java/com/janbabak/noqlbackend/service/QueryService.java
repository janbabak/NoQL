package com.janbabak.noqlbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.chat.ChatResponse;
import com.janbabak.noqlbackend.model.chat.CreateMessageWithResponseRequest;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.database.DatabaseStructure;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponseDto;
import com.janbabak.noqlbackend.model.query.QueryResponse;
import com.janbabak.noqlbackend.model.query.QueryResponse.QueryResult;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.service.api.GptApi;
import com.janbabak.noqlbackend.service.api.QueryApi;
import com.janbabak.noqlbackend.service.database.BaseDatabaseService;
import com.janbabak.noqlbackend.service.database.DatabaseServiceFactory;
import com.janbabak.noqlbackend.service.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.DATABASE;
import static com.janbabak.noqlbackend.model.query.QueryResponse.ColumnTypes;
import static com.janbabak.noqlbackend.model.query.QueryResponse.successfulResponse;
import static java.sql.Types.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryService {
    private final QueryApi queryApi = new GptApi();
    private final DatabaseRepository databaseRepository;
    private final Settings settings;
    private final ChatService chatService;
    private final ChatQueryWithResponseService chatQueryWithResponseService;
    private final PlotService plotService;

    /**
     * Create system query for the LLM that specifies what should be done, database schema, ...
     *
     * @param dbStructure structure of the database, e.g. create script (describes tables, columns, etc.)
     * @param database    database metadata object
     * @return generated query
     */
    @SuppressWarnings("all")
    public static String createSystemQuery(String dbStructure, Database database) {
        return new StringBuilder(
                "You are an assistant that translates natural language queries into a database query language.\n")
                .append(database.getIsSQL() ? "Write an SQL query for the " : "Write a query for the ")
                .append(database.getEngine().toString().toLowerCase(Locale.ROOT))
                .append("The response must contain only the transalted query without any additional text and markdown syntax.\n")
                .append("\nThis is the database structure:\n")
                .append(dbStructure)
                .append("Translate the user's queries.\n")
                .toString();
    }

    @SuppressWarnings("all")
    public static String createSystemQueryExperimental(String dbStructure, Database database) {
        // TODO: securly insert credentials from coresponding database
        // TOdO: parametrize the file name
        return new StringBuilder(
                """
                        You are an assistant that helps users visualise data. You have two functions. The first function
                        is translation of natural language queries into a database language. The second function is
                        visualising data. If the user wants to show or display or find or retrieve some data, translate
                        it into""")
                .append(database.getIsSQL() ? "an SQL query" : "an query language query")
                .append(" for the ")
                .append(database.getEngine().toString().toLowerCase(Locale.ROOT))
                .append("""
                        \ndatabase. I will use this query for displaying the data in form of table. If the user wants to
                        plot, chart or visualize the data, create a Python script that will select the data and
                        visualise them in a chart. Save the generated chart into a file called
                        plotService/plots/plot.png and don't show it. To connect to the database use host='localhost',
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
     * @param page     number of pages (first page has index 0), if null, default value is 0
     * @param pageSize number of items in one page,<br />
     *                 if null default value is defined by {@code PAGINATION_DEFAULT_PAGE_SIZE} env,<br />
     *                 max allowed size is defined by {@code PAGINATION_MAX_PAGE_SIZE} env
     * @param database database object
     * @return database language query with pagination
     * @throws BadRequestException value is greater than maximum allowed value
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
     * Sometimes the model does not return just the query itself, but puts it into a markdown and add some text. <br />
     * Extract the executable query from the response that can look like this: <br />
     * {@code Use the following command to retrieve all users. ```select * from public.user;```} <br />
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
        // I cannot cut only the ``` because it usually looks like ```sql\n or something like that
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

    /**
     * Classify columns into column types
     *
     * @param resultSet               query result
     * @param query                   generated query without pagination
     * @param queryResult             result object
     * @param specificDatabaseService specific database service capable of executing query
     * @return column types
     * @throws SQLException                TODO
     * @throws DatabaseConnectionException connection failure
     * @throws DatabaseExecutionException  execution fail
     */
    private ColumnTypes getColumnTypes(
            ResultSet resultSet,
            String query,
            QueryResult queryResult,
            BaseDatabaseService specificDatabaseService
    ) throws SQLException, DatabaseConnectionException, DatabaseExecutionException {
        ColumnTypes columnTypes = new ColumnTypes();
        for (String columnName : queryResult.getColumnNames()) {
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
     * @param id       database id
     * @param query    in natural query or database query language
     * @param page     page number (fist page starts by 0), if null, default value is 0
     * @param pageSize number of items in one page,<br />
     *                 default value is defined by {@code PAGINATION_DEFAULT_PAGE_SIZE} env,<br />
     *                 max allowed size is defined by {@code PAGINATION_MAX_PAGE_SIZE} env
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
        ChatQueryWithResponseDto message = new ChatQueryWithResponseDto( // TODO better solution
                UUID.randomUUID(), query, null, Timestamp.from(Instant.now())); // TODO: replace null
        try {
            ResultSet resultSet = specificDatabaseService.executeQuery(paginatedQuery);
            QueryResult queryResult = new QueryResult(resultSet);
            Long totalCount = getTotalCount(query, specificDatabaseService);

            return QueryResponse.successfulResponse(queryResult, message, totalCount);
        } catch (DatabaseExecutionException | SQLException e) {
            return QueryResponse.failedResponse(message, e.getMessage()); // TODO: better solution
        }
    }

    /**
     * Execute natural language select query from the chat. The query is translated to specific dialect via LLM
     * and then executed.
     * Select query is read only, and it returns a result that is automatically paginated starting by page 0.
     *
     * @param id           database id
     * @param queryRequest in natural query or database query language
     * @param pageSize     number of items in one page,<br />
     *                     default value is defined by {@code PAGINATION_DEFAULT_PAGE_SIZE} env,<br />
     *                     max allowed size is defined by {@code PAGINATION_MAX_PAGE_SIZE} env
     * @return query result
     * @throws LLMException                large language model failure
     * @throws BadRequestException         page size is greater than maximum allowed value
     * @throws EntityNotFoundException     queried database not found.
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws BadRequestException         requested page size is greater than maximum allowed value
     */
    public QueryResponse executeChat(UUID id, QueryRequest queryRequest, Integer pageSize)
            throws EntityNotFoundException, DatabaseConnectionException, DatabaseExecutionException,
            BadRequestException, LLMException, JsonProcessingException {

        log.info("Execute chat, database_id={}", id);

        Database database = databaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, id));

        BaseDatabaseService specificDatabaseService = DatabaseServiceFactory.getDatabaseService(database);
        DatabaseStructure databaseStructure = specificDatabaseService.retrieveSchema();
        String systemQuery = createSystemQuery(databaseStructure.generateCreateScript(), database);
        List<String> errors = new ArrayList<>();

        List<ChatQueryWithResponse> chatHistory = chatQueryWithResponseService.getMessagesFromChat(
                queryRequest.getChatId());
        for (int attempt = 1; attempt <= settings.translationRetries; attempt++) {
            String query = queryApi.queryModel(chatHistory, queryRequest.getQuery(), systemQuery, errors);
            // TODO: remove after testing
//            String query = """
//                    Use the following command to retrieve all users.
//                    ```
//                    select * from public.user;
//                    ```""";
            query = extractQueryFromMarkdownInResponse(query);
            String paginatedQuery = setPaginationInSqlQuery(query, 0, pageSize, database);

            try {
                ResultSet resultSet = specificDatabaseService.executeQuery(paginatedQuery);
                QueryResult queryResult = new QueryResult(resultSet);
                Long totalCount = getTotalCount(query, specificDatabaseService);

                ChatQueryWithResponse chatQueryWithResponse = chatService.addMessageToChat(
                        queryRequest.getChatId(),
                        new CreateMessageWithResponseRequest(queryRequest.getQuery(), query));

                ChatQueryWithResponseDto chatQueryWithResponseDto = null;
                try {
                    chatQueryWithResponseDto = new ChatQueryWithResponseDto(
                            chatQueryWithResponse, "/static/images/plot.png");
                } catch (JsonProcessingException e) {
                    errors.add("Your response cannot be parsed into JSON. Response is: "
                            + chatQueryWithResponse.getResponse());
                }
                return QueryResponse.successfulResponse(queryResult, chatQueryWithResponseDto, totalCount);
            } catch (DatabaseExecutionException | SQLException e) {
                log.info("Executing natural language query failed, attempt={}, paginatedQuery={}",
                        attempt, paginatedQuery);

                errors.add("Error occurred when during execution of your query.\n" +
                        "This is the error: " + e.getMessage() +
                        "This is the query: " + query);

                if (attempt == settings.translationRetries) {
                    // last try failed
                    ChatQueryWithResponse message = chatService.addMessageToChat(
                            queryRequest.getChatId(),
                            new CreateMessageWithResponseRequest(queryRequest.getQuery(), query));

                    // TODO: what if the chat query with response fails
                    return QueryResponse.failedResponse(
                            new ChatQueryWithResponseDto(message, "/static/images/plot.png"), e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Plot results of chat response
     *
     * @param chatResponse chat response
     * @param errors       collection of errors
     * @return true fi success, false otherwise
     * @throws IOException when script cannot be executed
     */
    private boolean plotResult(ChatResponse chatResponse, List<String> errors) throws IOException {
        log.info("Generate plot");
        String output = plotService.generatePlot(chatResponse.getPythonCode());
        if (output != null) {
            errors.add("Error when running the script. Script output is: " + output);
            return false;
        }
        return true;
    }

    private QueryResponse showResultTable(
            QueryRequest queryRequest,
            ChatResponse chatResponse,
            BaseDatabaseService specificDatabaseService,
            Database database,
            Integer pageSize,
            List<String> errors) throws BadRequestException, DatabaseConnectionException, DatabaseExecutionException,
            SQLException, EntityNotFoundException {

        String paginatedQuery = setPaginationInSqlQuery(chatResponse.getDatabaseQuery(), 0, pageSize, database);
        ResultSet resultSet = specificDatabaseService.executeQuery(paginatedQuery);
        QueryResult queryResult = new QueryResult(resultSet);
        Long totalCount = getTotalCount(chatResponse.getDatabaseQuery(), specificDatabaseService);
        ChatQueryWithResponse chatQueryWithResponse = chatService.addMessageToChat(
                queryRequest.getChatId(), new CreateMessageWithResponseRequest(
                        queryRequest.getQuery(), chatResponse.getDatabaseQuery()));

        ChatQueryWithResponseDto chatQueryWithResponseDto = null;
        try {
            chatQueryWithResponseDto = new ChatQueryWithResponseDto(chatQueryWithResponse, "/static/images/plot.png"); // TODO: redundant json parsing
        } catch (JsonProcessingException e) {
            errors.add("Your response cannot be parsed into JSON. Response is: "
                    + chatQueryWithResponse.getResponse());
        }
        return QueryResponse.successfulResponse(queryResult, chatQueryWithResponseDto, totalCount);
    }

    public QueryResponse executeChatExperimental(UUID id, QueryRequest queryRequest, Integer pageSize)
            throws EntityNotFoundException, DatabaseConnectionException, DatabaseExecutionException,
            LLMException, JsonProcessingException {

        log.info("Execute chat, database_id={}", id);

        Database database = databaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, id));

        BaseDatabaseService specificDatabaseService = DatabaseServiceFactory.getDatabaseService(database);
        DatabaseStructure databaseStructure = specificDatabaseService.retrieveSchema();
        String systemQuery = createSystemQueryExperimental(databaseStructure.generateCreateScript(), database);
        List<String> errors = new ArrayList<>();

        List<ChatQueryWithResponse> chatHistory =
                chatQueryWithResponseService.getMessagesFromChat(queryRequest.getChatId());

        for (int attempt = 1; attempt <= settings.translationRetries; attempt++) {
            String chatResponseString = queryApi.queryModel(chatHistory, queryRequest.getQuery(), systemQuery, errors);
            ChatResponse chatResponse;

            try {
                chatResponse = JsonUtils.createChatResponse(chatResponseString);

                // plot result
                if (chatResponse.getGeneratePlot()) {
                    if (plotResult(chatResponse, errors)) {
                        ChatQueryWithResponse chatQueryWithResponse = chatService.addMessageToChat(
                                queryRequest.getChatId(), new CreateMessageWithResponseRequest(
                                        queryRequest.getQuery(), chatResponseString));

                        ChatQueryWithResponseDto chatQueryWithResponseDto =
                                new ChatQueryWithResponseDto(chatQueryWithResponse, "/static/images/plot.png"); // TODO: redundant json parsing

                        return successfulResponse(null, chatQueryWithResponseDto, null);
                    }
                }
                else {
                    return showResultTable(
                            queryRequest, chatResponse, specificDatabaseService, database, pageSize, errors);
                }
            } catch (JsonProcessingException e) {
                errors.add("Cannot parse response JSON - bad syntax.");
            } catch (BadRequestException | SQLException e) {
                log.info("Executing natural language query failed, attempt={}", attempt);

                errors.add("Error occurred when during execution of your query.\n" +
                        "This is the error: " + e.getMessage());

                // last try failed
                if (attempt == settings.translationRetries) {
                    ChatQueryWithResponse message = chatService.addMessageToChat(
                            queryRequest.getChatId(),
                            new CreateMessageWithResponseRequest(queryRequest.getQuery(), chatResponseString));

                    // TODO: what if the chat query with response fails
                    return QueryResponse.failedResponse(
                            new ChatQueryWithResponseDto(message, null),
                            e.getMessage());
                }
            } catch (IOException e) {
                System.out.println(e.getMessage()); // TODO: handle
            }
        }
        return null;
    }
}
