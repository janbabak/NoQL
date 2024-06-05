package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryServiceTest {
    @InjectMocks
    private QueryService queryService;

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
        UUID chatId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
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

        // TODO: remove embedded credentials after it is fixed in QueryService
        String expectedSystemQuery = """
                You are an assistant that helps users visualise data. You have two functions. The first function
                is translation of natural language queries into a database language. The second function is
                visualising data. If the user wants to show or display or find or retrieve some data, translate
                it into an SQL query for the postgres database. I will use this query for displaying the data in form of table. If the user wants to
                plot, chart or visualize the data, create a Python script that will select the data and
                visualise them in a chart. Save the generated chart into a file called ./plotService/plots/123e4567-e89b-12d3-a456-426614174000.png and don't show it.
                To connect to the database use host='localhost',
                port=5432, user='user', password='password', database='database'.
                
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
        String actualSystemQuery = QueryService.createSystemQuery(dbStructure, postgresDatabase, chatId);

        // then
        assertEquals(expectedSystemQuery, actualSystemQuery);
    }

    @ParameterizedTest
    @MethodSource("setPaginationDataProvider")
    @DisplayName("Test set pagination")
    void testSetPagination(String query, Integer page, Integer pageSize, String expectedQuery) throws BadRequestException {
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
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        8,
                        15,
                        // language=SQL
                        "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) LIMIT 15 OFFSET 120;"
                },
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        null,
                        null,
                        // language=SQL
                        "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) LIMIT 10 OFFSET 0;"
                },
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        4,
                        null,
                        // language=SQL
                        "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) LIMIT 10 OFFSET 40;"
                }
        };
    }


    @Disabled // TODO: fix
    @ParameterizedTest
    @MethodSource("paginationTestMethodSource")
    void setPaginationTestInSqlDatabase(
            String query,
            Integer page,
            Integer pageSize,
            String expectedQuery
    ) throws BadRequestException {
        String actualValue = queryService.setPaginationInSqlQuery(query, page, pageSize, postgresDatabase);
        assertEquals(expectedQuery, actualValue);
    }

    private static Object[][] paginationTestMethodSource() {
        return new Object[][]{
                // fist page, no offset
                {
                        "SELECT name FROM product WHERE price < 1000;",
                        null,
                        15,
                        """
                        SELECT name FROM product WHERE price < 1000
                        LIMIT 15;"""
                },
                // n-th page, set offset ond limit
                {
                        "SELECT name FROM product WHERE price < 1000;",
                        8,
                        15,
                        """
                        SELECT name FROM product WHERE price < 1000
                        LIMIT 15
                        OFFSET 120;"""
                },
                // override offset, add limit
                {
                        "SELECT name FROM product WHERE price < 1000 OFFSET 4;",
                        8,
                        15,
                        """
                        SELECT name FROM product WHERE price < 1000 OFFSET 120
                        LIMIT 15;""",
                },
                // override offset, add limit, offset on new line
                {
                        """
                        SELECT name FROM product
                        WHERE price < 1000
                        OFFSET 4;""",
                        8,
                        15,
                        """
                        SELECT name FROM product
                        WHERE price < 1000
                        OFFSET 120
                        LIMIT 15;""",
                },
                // limit already used - not override it, override offset
                {
                        """
                        SELECT *
                        FROM public.user
                        ORDER BY created_at ASC
                        LIMIT 1;""",
                        3,
                        10,
                        """
                        SELECT *
                        FROM public.user
                        ORDER BY created_at ASC
                        LIMIT 1
                        OFFSET 30;""",
                },
                // limit already used - override it, override offset
                {
                        """
                        SELECT *
                        FROM public.user
                        ORDER BY created_at ASC
                        LIMIT 1;""",
                        0,
                        10,
                        """
                        SELECT *
                        FROM public.user
                        ORDER BY created_at ASC
                        LIMIT 10
                        OFFSET 0;""",
                },
                // limit already used - not override it, override offset, no new lines
                {
                        "SELECT * FROM public.user ORDER BY created_at ASC LIMIT 1;",
                        3,
                        10,
                        """
                        SELECT * FROM public.user ORDER BY created_at ASC LIMIT 1
                        OFFSET 30;""",
                },
                // limit already used - override it, no semicolon, override offset
                {
                        """
                        SELECT *
                        FROM public.user
                        ORDER BY created_at ASC
                        LIMIT 19""",
                        3,
                        10,
                        """
                        SELECT *
                        FROM public.user
                        ORDER BY created_at ASC
                        LIMIT 10
                        OFFSET 30;""",
                },
                // limit and offset already used, override them both
                {
                        "SELECT name FROM product LIMIT 50 OFFSET 4;",
                        7,
                        19,
                        "SELECT name FROM product LIMIT 19 OFFSET 133;"
                },
                // limit and offset already used, offset before limit, override them all
                {
                        "SELECT name FROM product OFFSET 4 LIMIT 50;",
                        7,
                        19,
                        "SELECT name FROM product OFFSET 133 LIMIT 19;"
                },
                // limit already used with value greater than allowed limit
                {
                        "SELECT name FROM product LIMIT 260",
                        null,
                        250,
                        "SELECT name FROM product LIMIT 250;"
                }
        };
    }

    @ParameterizedTest
    @MethodSource("extractQueryFromMarkdownInResponseTestMethodSource")
    void extractQueryFromMarkdownInResponseTest(String response, String expectedQuery) {
        String actualQuery = queryService.extractQueryFromMarkdownInResponse(response);
        assertEquals(expectedQuery, actualQuery);
    }

    private static Object[][] extractQueryFromMarkdownInResponseTestMethodSource() {
        return new Object[][]{
                // responses with markdown
                {
                        """
                        Use the following command to retrieve all users.
                        ```
                        select * from public.user;
                        ```""",
                        // language=SQL
                        "select * from public.user;"
                },
                {
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
                }
        };
    }
}