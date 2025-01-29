package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.query.ChatResponse;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.service.database.QueryDAO;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import com.janbabak.noqlbackend.service.user.UserService;
import com.janbabak.noqlbackend.service.QueryService.PaginatedQuery;
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
    private DatabaseRepository databaseRepository;

    @Mock
    @SuppressWarnings("unused") // used internally
    private ChatRepository chatRepository;

    @Mock
    private ChatQueryWithResponseRepository chatQueryWithResponseRepository;

    @Mock
    private UserService userService;

    @Mock
    @SuppressWarnings("unused") // used internally
    AuthenticationService authenticationService;

    @Mock
    @SuppressWarnings("unused") // used internally
    QueryDAO queryDAO;

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
                You are an assistant that helps users visualise data. You have two functions. The first function
                is translation of natural language queries into a database language. The second function is
                visualising data. If the user wants to show or display or find or retrieve some data, translate
                it into an SQL query for the postgres database. Generate this query nicely formatted with line breaks.
                I will use this query for displaying the data in form of table. If the user wants to plot,
                chart or visualize the data, create a Python script that will select the data and visualise them
                in a chart. Save the generated chart into a file called ./plotService/plots/noQlGeneratedPlot.png and don't show it.
                To connect to the database use host='localhost', port=1111111111 , user='admin4445900234', password='dkl45349?405', database='database99889899'.
                
                Your response must be in JSON format
                { databaseQuery: string, generatePlot: boolean, pythonCode: string }.
                
                The database structure looks like this:CREATE SCHEMA IF NOT EXISTS public;
                
                CREATE TABLE IF NOT EXISTS public.user
                (
                    id integer,
                    name character varying,
                    surname character varying,
                    age integer
                );""";

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

    @ParameterizedTest
    @MethodSource("testExtractQueryFromMarkdownResponseDataProvider")
    @DisplayName("Test extract query from markdown response")
    void testExtractQueryFromMarkdownResponse(String response, String expectedQuery) {
        // when
        String actualQuery = queryService.extractQueryFromMarkdownInResponse(response);

        // then
        assertEquals(expectedQuery, actualQuery);
    }

    @SuppressWarnings("all")
    private static Object[][] testExtractQueryFromMarkdownResponseDataProvider() {
        return new Object[][]{
                // incorrect response without markdown
                {
                        "It looks like you are using a Postgresql database.",
                        "It looks like you are using a Postgresql database."
                },
                // real response without python script
                {
                        // language=MD
                        """
                        ```{
                          "databaseQuery": "SELECT * FROM public.database ORDER BY id ASC LIMIT 1;",
                          "generatePlot": false,
                          "pythonCode": ""
                        }```
                        """,
                        // language=JSON
                        """
                        {
                          "databaseQuery": "SELECT * FROM public.database ORDER BY id ASC LIMIT 1;",
                          "generatePlot": false,
                          "pythonCode": ""
                        }"""
                },
                {
                        // language=MD
                        """
                        ```
                        {
                          "databaseQuery": "SELECT * FROM public.database ORDER BY id ASC LIMIT 1;",
                          "generatePlot": false,
                          "pythonCode": ""
                        }```
                        """,
                        // language=JSON
                        """
                        {
                          "databaseQuery": "SELECT * FROM public.database ORDER BY id ASC LIMIT 1;",
                          "generatePlot": false,
                          "pythonCode": ""
                        }"""
                },
                {
                        // language=MD
                        """
                        ```json{
                          "databaseQuery": "SELECT * FROM public.database ORDER BY id ASC LIMIT 1;",
                          "generatePlot": false,
                          "pythonCode": ""
                        }```
                        """,
                        // language=JSON
                        """
                        {
                          "databaseQuery": "SELECT * FROM public.database ORDER BY id ASC LIMIT 1;",
                          "generatePlot": false,
                          "pythonCode": ""
                        }"""
                },
                {
                        // language=MD
                        """
                        ```json
                        {
                          "databaseQuery": "SELECT * FROM public.database ORDER BY id ASC LIMIT 1;",
                          "generatePlot": false,
                          "pythonCode": ""
                        }```
                        """,
                        // language=JSON
                        """
                        {
                          "databaseQuery": "SELECT * FROM public.database ORDER BY id ASC LIMIT 1;",
                          "generatePlot": false,
                          "pythonCode": ""
                        }"""
                },
                // real response without python script, no new line at the end
                {
                        // language=MD
                        """
                        ```json
                        {
                          "databaseQuery": "SELECT * FROM public.database ORDER BY id ASC LIMIT 1;",
                          "generatePlot": false,
                          "pythonCode": ""
                        }```""",
                        // language=JSON
                        """
                        {
                          "databaseQuery": "SELECT * FROM public.database ORDER BY id ASC LIMIT 1;",
                          "generatePlot": false,
                          "pythonCode": ""
                        }"""
                },
                // real response with python script
                {
                        // language=MD
                        """
                        ```json
                        {
                           "databaseQuery": "SELECT sex, COUNT(id) as count FROM public.user WHERE age > 24 GROUP BY sex;",
                           "generatePlot": true,
                           "pythonCode": "import psycopg2\\nimport matplotlib.pyplot as plt\\n\\n# Database connection parameters\\nconn_params = {\\n    'host': 'localhost',\\n    'port': 5432,\\n    'user': 'user',\\n    'password': 'password',\\n    'database': 'database'\\n}\\n\\n# SQL query to get the data\\nquery = \\"SELECT sex, COUNT(id) as count FROM public.user WHERE age > 24 GROUP BY sex;\\"\\n\\n# Connect to the database and fetch data\\nconn = psycopg2.connect(**conn_params)\\ncur = conn.cursor()\\ncur.execute(query)\\nrows = cur.fetchall()\\ncur.close()\\nconn.close()\\n\\n# Prepare data for plotting\\nlabels = [row[0] for row in rows]\\nsizes = [row[1] for row in rows]\\n\\n# Plotting\\nfig, axs = plt.subplots(2, 1, figsize=(10, 12))\\n\\n# Pie chart\\naxs[0].pie(sizes, labels=labels, autopct='%1.1f%%', startangle=140)\\naxs[0].axis('equal')  # Equal aspect ratio ensures that pie is drawn as a circle.\\naxs[0].set_title('User Sex Distribution (Age > 24)')\\n\\n# Bar chart\\naxs[1].bar(labels, sizes, color=['blue', 'orange'])\\naxs[1].set_title('User Sex Distribution (Age > 24)')\\naxs[1].set_xlabel('Sex')\\naxs[1].set_ylabel('Count')\\n\\n# Save plot\\nplt.tight_layout()\\nplt.savefig('./plotService/plots/f40115c9-7838-4f52-936a-be5b53d42e15.png')"
                         }
                         ```""",
                        // language=JSON
                        """
                        {
                           "databaseQuery": "SELECT sex, COUNT(id) as count FROM public.user WHERE age > 24 GROUP BY sex;",
                           "generatePlot": true,
                           "pythonCode": "import psycopg2\\nimport matplotlib.pyplot as plt\\n\\n# Database connection parameters\\nconn_params = {\\n    'host': 'localhost',\\n    'port': 5432,\\n    'user': 'user',\\n    'password': 'password',\\n    'database': 'database'\\n}\\n\\n# SQL query to get the data\\nquery = \\"SELECT sex, COUNT(id) as count FROM public.user WHERE age > 24 GROUP BY sex;\\"\\n\\n# Connect to the database and fetch data\\nconn = psycopg2.connect(**conn_params)\\ncur = conn.cursor()\\ncur.execute(query)\\nrows = cur.fetchall()\\ncur.close()\\nconn.close()\\n\\n# Prepare data for plotting\\nlabels = [row[0] for row in rows]\\nsizes = [row[1] for row in rows]\\n\\n# Plotting\\nfig, axs = plt.subplots(2, 1, figsize=(10, 12))\\n\\n# Pie chart\\naxs[0].pie(sizes, labels=labels, autopct='%1.1f%%', startangle=140)\\naxs[0].axis('equal')  # Equal aspect ratio ensures that pie is drawn as a circle.\\naxs[0].set_title('User Sex Distribution (Age > 24)')\\n\\n# Bar chart\\naxs[1].bar(labels, sizes, color=['blue', 'orange'])\\naxs[1].set_title('User Sex Distribution (Age > 24)')\\naxs[1].set_xlabel('Sex')\\naxs[1].set_ylabel('Count')\\n\\n# Save plot\\nplt.tight_layout()\\nplt.savefig('./plotService/plots/f40115c9-7838-4f52-936a-be5b53d42e15.png')"
                         }"""
                }
        };
    }

    @Test
    @DisplayName("Test extract query from markdown response bad request")
    @SuppressWarnings("all")
    void testExtractQueryFromMarkdownResponseBadRequest() {
        assertThrows(NullPointerException.class, () -> queryService.extractQueryFromMarkdownInResponse(null));
    }

    @Test
    @DisplayName("Test execute query - language query database not found")
    void testExecuteQueryLanguageQueryDatabaseNotFound() {
        // given
        String query = "SELECT * FROM public.user;";
        UUID databaseId = UUID.randomUUID();

        when(databaseRepository.findById(databaseId)).thenReturn(Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> queryService.executeQueryLanguageSelectQuery(databaseId, query, 0, 10));

        // then
        assertEquals("Database of id: \"" + databaseId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Test load message data - message not found")
    void testGetDataByMessageIdNotFound() {
        // given
        UUID messageId = UUID.randomUUID();
        int page = 0;
        String expectedErrorMsg = "Message of id: \"" + messageId + "\" not found.";

        when(chatQueryWithResponseRepository.findById(messageId)).thenReturn(Optional.empty());

        // then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> queryService.getDataByMessageId(messageId, page, 10));

        assertEquals(expectedErrorMsg, exception.getMessage());
    }


    @ParameterizedTest
    @MethodSource("testGetDataByMessageIdLlmResponseHasEmptyQueryTestDataProvider")
    @DisplayName("Test load message data - LLM response has empty query")
    void testGetDataByMessageIdLlmResponseHasEmptyQueryTest(String llmResponse) throws EntityNotFoundException {

        // given
        UUID messageId = UUID.randomUUID();

        ChatQueryWithResponse chatQueryWithResponse = ChatQueryWithResponse.builder()
                .id(messageId)
                .chat(Chat.builder()
                        .id(UUID.randomUUID())
                        .database(postgresDatabase)
                        .build())
                .llmResponse(llmResponse)
                .build();

        when(chatQueryWithResponseRepository.findById(messageId)).thenReturn(Optional.of(chatQueryWithResponse));

        // then
        assertNull(queryService.getDataByMessageId(messageId, 0, 10));
    }

    static Object[][] testGetDataByMessageIdLlmResponseHasEmptyQueryTestDataProvider() {
        return new Object[][]{
                {
                        null
                },
                {
                        ""
                },
                {
                        "{}"
                },
                {
                        // language=JSON
                        """
                                {
                                  "databaseQuery": "",
                                  "generatePlot": false,
                                  "pythonCode": ""
                                }"""
                }
        };
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

        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(postgresDatabase));

        // then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> queryService.queryChat(databaseId, chatId, request, pageSize));

        assertEquals(expectedErrorMsg, exception.getMessage());
    }

    @Test
    @DisplayName("Test query chat - query limit exceeded")
    void testQueryChatQueryLimitExceeded() throws EntityNotFoundException, DatabaseConnectionException,
            DatabaseExecutionException, LLMException, BadRequestException {
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

        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(database));
        when(userService.decrementQueryLimit(any())).thenReturn(0);
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        // when
        ChatResponse actual = queryService.queryChat(databaseId, chatId, request, 10);

        // then
        assertEquals(expected, actual);
    }
}