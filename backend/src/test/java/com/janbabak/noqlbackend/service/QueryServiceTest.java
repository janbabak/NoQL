package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.query.ChatResponse;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.service.database.MessageDataDAO;
import com.janbabak.noqlbackend.service.query.QueryService;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import com.janbabak.noqlbackend.service.user.UserService;
import com.janbabak.noqlbackend.service.query.QueryService.PaginatedQuery;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * QueryService unit tests.
 */
@ExtendWith(MockitoExtension.class)
class QueryServiceTest {
    @InjectMocks
    private QueryService queryService;

    @Mock
    private DatabaseRepository databaseRepositoryMock;

    @Mock
    @SuppressWarnings("unused") // used internally
    private ChatRepository chatRepositoryMock;

    @Mock
    private UserService userServiceMock;

    @Mock
    @SuppressWarnings("unused") // used internally
    AuthenticationService authenticationServiceMock;

    @Mock
    @SuppressWarnings("unused") // used internally
    MessageDataDAO messageDataDAOMock;

    private final Database postgresDatabase;

    public QueryServiceTest() {
        User testUser = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("test@gmail.com")
                .password("password")
                .build();
        postgresDatabase = new Database(
                UUID.randomUUID(),
                "Postgres db",
                "localhost",
                5432,
                "database",
                "jan",
                "4530958340??",
                DatabaseEngine.POSTGRES,
                List.of(),
                testUser);
    }

    @Test
    @DisplayName("Test create system query")
    void testCreateSystemQueryTest() {
        // given
        // language=SQL
        String dbStructure = """
                CREATE SCHEMA IF NOT EXISTS public;
                
                CREATE TABLE IF NOT EXISTS public.user
                (
                    id integer,
                    name character varying,
                    surname character varying,
                    age integer
                );""";

        String expectedSystemQuery = """
                You are an AI agent that helps users with data analysis and visualisation by translating their requests
                in natural language into database queries and Python scripts for data visualisation.
                
                You have the following tools at your disposal:
                
                The first function is 'executeQuery' that executes query in valid database query language. In
                this case SQL query for the postgres database. If users asks for data, you can generate a
                query based on their input in natural language, call this function with the query as parameter. The
                system will present retrieved data to users in form of table. Generate this query nicely formatted with
                line breaks so it can be presented to users with the data table.
                
                The second function is 'generatePlot' that creates a plot from data in the database. If users
                want to visualize data to see them in form of chart such as pie chart, bar chart, line chart,
                scatter plot, and others charts, you can generate a Python script that selects the data and visualize
                them in a chart using the matplotlib library. Save the generated chart into a file called
                ./plotService/plots/noQlGeneratedPlot.png and don't show it. This Python script will be executed and the resulting
                plot will be presented to users, don't worry about the presentation. To connect to the database use
                host='localhost', port=1111111111, user='admin4445900234', password='dkl45349?405', database='database99889899'.
                
                Users may ask for just one of these two functions or both of them. If users ask for data, the first
                function should be called. If users ask for plot, the second function should be called. If user asks for
                data visualisation in form of chart and he can benefit from seeing the data in form of table, you can
                call both functions. If users want to see a chart but don't specify the type of chart, choose the most
                suitable chart type based on the data and context.
                
                Response with brief explanation of the results to help users understand them better.
                
                To help you generate better queries and plots, here is the structure of the database:
                CREATE SCHEMA IF NOT EXISTS public;
                
                CREATE TABLE IF NOT EXISTS public.user
                (
                    id integer,
                    name character varying,
                    surname character varying,
                    age integer
                );
                """;

        // when
        String actualSystemQuery = QueryService.createSystemQuery(dbStructure, postgresDatabase);

        // then
        assertEquals(expectedSystemQuery, actualSystemQuery);
    }

    @ParameterizedTest
    @MethodSource("setPaginationDataProvider")
    @DisplayName("Test set pagination")
    void testSetPagination(String query, Integer page, Integer pageSize, PaginatedQuery expectedQuery)
            throws BadRequestException {

        MockedStatic<Settings> settingsMockedStatic = mockStatic(Settings.class);
        settingsMockedStatic.when(Settings::getMaxPageSizeStatic).thenReturn(50);
        if (pageSize == null) {
            settingsMockedStatic.when(Settings::getDefaultPageSizeStatic).thenReturn(10);
        }

        // when
        PaginatedQuery actualValue = QueryService.setPaginationInSqlQuery(
                query, page, pageSize, postgresDatabase);

        // then
        assertEquals(expectedQuery, actualValue);

        settingsMockedStatic.close();
    }

    @SuppressWarnings("all")
    static Object[][] setPaginationDataProvider() {
        return new Object[][]{
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;", // trailing semicolon
                        8,
                        15,
                        new PaginatedQuery(
                                // language=SQL
                                "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) AS query LIMIT 15 OFFSET 120;",
                                8,
                                15)
                },
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3", // no trailing semicolon
                        8,
                        15,
                        new PaginatedQuery(
                                // language=SQL
                                "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) AS query LIMIT 15 OFFSET 120;",
                                8,
                                15)
                },
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        null, // null page
                        null, // null page size
                        new PaginatedQuery(
                                // language=SQL
                                "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) AS query LIMIT 10 OFFSET 0;",
                                0,
                                10)
                },
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        4,
                        null, // null page size
                        new PaginatedQuery(
                                // language=SQL
                                "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) AS query LIMIT 10 OFFSET 40;",
                                4,
                                10)
                }
        };
    }

    @ParameterizedTest
    @MethodSource("setPaginationBadRequestDataProvider")
    @DisplayName("Test set pagination with bad request")
    void testSetPaginationBadRequest(String query, Integer page, Integer pageSize, String errorMessage) {
        // given
        MockedStatic<Settings> settingsMockedStatic = mockStatic(Settings.class);
        if (page >= 0) { // otherwise unnecessary stubbing error
            settingsMockedStatic.when(Settings::getMaxPageSizeStatic).thenReturn(50);
        }

        // when
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> QueryService.setPaginationInSqlQuery(query, page, pageSize, postgresDatabase));

        // then
        assertEquals(errorMessage, exception.getMessage());

        settingsMockedStatic.close();
    }

    @SuppressWarnings("all")
    static Object[][] setPaginationBadRequestDataProvider() {
        return new Object[][]{
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        5,
                        60,
                        "Page size is greater than maximum allowed value=50"
                },
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        -5,
                        15,
                        "Page number cannot be negative, page=-5"
                }
        };
    }

    @ParameterizedTest
    @MethodSource("trimAndRemoveTrailingSemicolonDataProvider")
    @DisplayName("Test trim and remove trailing semicolon")
    void testTrimAndRemoveTrailingSemicolon(String query, String expectedQuery) {
        // when
        String actualValue = QueryService.trimAndRemoveTrailingSemicolon(query);

        // then
        assertEquals(expectedQuery, actualValue);
    }

    @SuppressWarnings("all")
    static Object[][] trimAndRemoveTrailingSemicolonDataProvider() {
        return new Object[][]{
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3"
                },
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3",
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3"
                },
                {
                        // language=SQL
                        "\nSELECT name FROM cvut.student WHERE grade < 3;\t",
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3"
                },
                {
                        // language=SQL
                        "   SELECT name FROM cvut.student WHERE grade < 3;\n",
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3"
                },
                {
                        // language=SQL
                        "\tSELECT name FROM cvut.student WHERE grade < 3; ",
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3"
                },
                {
                        // language=SQL
                        "\nSELECT name FROM cvut.student WHERE grade < 3;    \n",
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3"
                },
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3\t   \n;    \n",
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3"
                },
                {
                        "",
                        ""
                }
        };
    }

    @Test
    @DisplayName("Test execute query - language query database not found")
    void testExecuteQueryLanguageQueryDatabaseNotFound() {
        // given
        String query = "SELECT * FROM public.user;";
        UUID databaseId = UUID.randomUUID();

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> queryService.executeQueryLanguageSelectQuery(databaseId, query, 0, 10));

        // then
        assertEquals("Database of id: \"" + databaseId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Test query chat - chat not found")
    void testQueryChatChatNotFound() {
        // given
        UUID chatId = UUID.randomUUID();
        UUID databaseId = UUID.randomUUID();
        Integer pageSize = 10;
        QueryRequest request = new QueryRequest("find all users", "gpt-4o");

        String expectedErrorMsg = "Chat of id: \"" + chatId + "\" not found.";

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.of(postgresDatabase));

        // then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> queryService.queryChat(databaseId, chatId, request, pageSize));

        assertEquals(expectedErrorMsg, exception.getMessage());
    }

    @Test
    @DisplayName("Test query chat - query limit exceeded")
    void testQueryChatQueryLimitExceeded() throws EntityNotFoundException, DatabaseConnectionException,
            DatabaseExecutionException {
        // given
        UUID databaseId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();

        QueryRequest request = new QueryRequest("find all users", "gpt-4o");

        User user = User.builder()
                .id(UUID.randomUUID())
                .queryLimit(0)
                .build();

        Database database = Database.builder()
                .id(databaseId)
                .user(user)
                .build();

        Chat chat = Chat.builder()
                .id(chatId)
                .build();

        ChatResponse expected = ChatResponse.failedResponse("Query limit exceeded", "find all users");

        when(databaseRepositoryMock.findById(databaseId)).thenReturn(Optional.of(database));
        when(userServiceMock.decrementQueryLimit(any())).thenReturn(0);
        when(chatRepositoryMock.findById(chatId)).thenReturn(Optional.of(chat));

        // when
        ChatResponse actual = queryService.queryChat(databaseId, chatId, request, 10);

        // then
        assertEquals(expected, actual);
    }
}