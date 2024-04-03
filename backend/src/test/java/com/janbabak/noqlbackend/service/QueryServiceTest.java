package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.model.database.Database;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class QueryServiceTest {
    @InjectMocks
    private QueryService queryService;

    private final Database postgresDatabase;

    public QueryServiceTest() {
        postgresDatabase = new Database(
                UUID.randomUUID(),
                "Postgres db",
                "host", 5432,
                "database",
                "user",
                "password",
                DatabaseEngine.POSTGRES,
                true);
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