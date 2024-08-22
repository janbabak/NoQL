package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.model.query.QueryResponse;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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
    private ChatQueryWithResponseRepository chatQueryWithResponseRepository;

    @Mock
    private Settings settings;

    private final Database postgresDatabase;

    public QueryServiceTest() {
        postgresDatabase = new Database(
                UUID.randomUUID(),
                "Postgres db",
                "localhost",
                5432,
                "database",
                "jan",
                "4530958340??",
                DatabaseEngine.POSTGRES,
                List.of());
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
                it into an SQL query for the postgres database. I will use this query for displaying the data in form of table. If the user wants to
                plot, chart or visualize the data, create a Python script that will select the data and
                visualise them in a chart. Save the generated chart into a file called ./plotService/plots/noQlGeneratedPlot.png and don't show it.
                To connect to the database use host='localhost', port=ppp45345ppp , user='admin4445900234', password='dkl45349?405', database='database99889899'.

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
    void testSetPagination(String query, Integer page, Integer pageSize, String expectedQuery)
            throws BadRequestException {

        // when
        when(settings.getMaxPageSize()).thenReturn(50);
        if (pageSize == null) {
            when(settings.getDefaultPageSize()).thenReturn(10);
        }
        String actualValue = queryService.setPaginationInSqlQuery(query, page, pageSize, postgresDatabase);

        // then
        assertEquals(expectedQuery, actualValue);
    }

    @SuppressWarnings("all")
    static Object[][] setPaginationDataProvider() {
        return new Object[][]{
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;", // trailing semicolon
                        8,
                        15,
                        // language=SQL
                        "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) AS query LIMIT 15 OFFSET 120;"
                },
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3", // no trailing semicolon
                        8,
                        15,
                        // language=SQL
                        "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) AS query LIMIT 15 OFFSET 120;"
                },
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        null, // null page
                        null, // null page size
                        // language=SQL
                        "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) AS query LIMIT 10 OFFSET 0;"
                },
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        4,
                        null, // null page size
                        // language=SQL
                        "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) AS query LIMIT 10 OFFSET 40;"
                }
        };
    }

    @ParameterizedTest
    @MethodSource("setPaginationBadRequestDataProvider")
    @DisplayName("Test set pagination with bad request")
    void testSetPaginationBadRequest(String query, Integer page, Integer pageSize, String errorMessage) {
        // given
        if (page >= 0) { // otherwise unnecessary stubbing error
            when(settings.getMaxPageSize()).thenReturn(50);
        }

        // when
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> queryService.setPaginationInSqlQuery(query, page, pageSize, postgresDatabase));

        // then
        assertEquals(errorMessage, exception.getMessage());
    }

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
        String actualValue = queryService.trimAndRemoveTrailingSemicolon(query);

        // then
        assertEquals(expectedQuery, actualValue);
    }

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

    private static Object[][] testExtractQueryFromMarkdownResponseDataProvider() {
        return new Object[][]{
                // responses with markdown
                {
                        // language=MD
                        """
                        Use the following command to retrieve all users.
                        ```
                        select * from public.user;
                        ```""",
                        // language=SQL
                        "select * from public.user;"
                },
                {
                        // language=MD
                        """
                        Here is the SQL query that retrieves all users from the 'user' table:
                        
                        ```sql
                        SELECT * FROM public.user;
                        ```\s
                        
                        This query selects all columns for all records in the 'user' table in the 'public' schema.""",
                        // language=SQL
                        "SELECT * FROM public.user;"
                },
                {
                        // language=MD
                        """
                        To select the names of all users older than 30 and sort them by name, you can use the following SQL query:
                        
                        ```sql
                        SELECT name
                        FROM public.user
                        WHERE age > 30
                        ORDER BY name;
                        ```\s
                        
                        This query selects the name of users from the 'user' table in the 'public' schema where the age is greater than 30 and then sorts the result by name in ascending order.""",
                        // language=SQL
                        """
                        SELECT name
                        FROM public.user
                        WHERE age > 30
                        ORDER BY name;"""
                },
                // correct response with proper sql
                {
                        // language=SQL
                        """
                        SELECT name
                        FROM public.user
                        WHERE age > 30
                        ORDER BY name DESC;""",
                        // language=SQL
                        """
                        SELECT name
                        FROM public.user
                        WHERE age > 30
                        ORDER BY name DESC;"""
                },
                // incorrect response without markdown
                {
                        "It looks like you are using a Postgresql database.",
                        "It looks like you are using a Postgresql database."
                },
                // real response without python script
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
    @DisplayName("Test execute query-language query database not found")
    void testExecuteQueryLanguageQueryDatabaseNotFound() {
        // given
        String query = "SELECT * FROM public.user;";
        UUID databaseId = UUID.randomUUID();

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class,
                () -> queryService.executeQueryLanguageSelectQuery(databaseId, query, 0, 10));
    }

    @Test
    @DisplayName("Test load chat result chat not found")
    void testLoadChatResultTestNotFound()
            throws DatabaseConnectionException, BadRequestException, EntityNotFoundException {

        // given
        UUID chatId = UUID.randomUUID();
        UUID databaseId = UUID.randomUUID();

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.of(postgresDatabase));
        when(chatQueryWithResponseRepository.findLatestMessageFromChat(chatId)).thenReturn(Optional.empty());
        QueryResponse actual = queryService.loadChatResult(databaseId, chatId, 0, 10);

        // then
        assertNull(actual);
    }

    @Test
    @DisplayName("Test execute chat database not found")
    void testExecuteChatDatabaseNotFound() {
        // given
        UUID databaseId = UUID.randomUUID();
        QueryRequest request = new QueryRequest(UUID.randomUUID(), "SELECT * FROM public.user;", "gpt-4o");

        // when
        when(databaseRepository.findById(databaseId)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> queryService.executeChat(databaseId, request, 10));
    }
}