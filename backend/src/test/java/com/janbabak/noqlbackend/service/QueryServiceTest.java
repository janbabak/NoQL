package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.model.database.Database;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
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

    @ParameterizedTest
    @MethodSource("paginationTestMethodSource")
    void setPaginationTestInSqlDatabase(
            String query,
            Integer page,
            Integer pageSize,
            Boolean setOffset,
            String expectedQuery
    ) throws Exception {
        String actualValue = queryService.setPagination(query, page, pageSize, postgresDatabase, setOffset);
        assertEquals(expectedQuery, actualValue);
    }

    private static Object[][] paginationTestMethodSource() {
        return new Object[][]{
                // fist page, no offset
                {
                        "SELECT name FROM product WHERE price < 1000;",
                        null,
                        15,
                        false,
                        """
                        SELECT name FROM product WHERE price < 1000
                        LIMIT 15;"""
                },
                // n-th page, set offset ond limit
                {
                        "SELECT name FROM product WHERE price < 1000;",
                        8,
                        15,
                        true,
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
                        true,
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
                        true,
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
                        true,
                        """
                        SELECT *
                        FROM public.user
                        ORDER BY created_at ASC
                        LIMIT 1
                        OFFSET 30;""",
                },
                // limit already used - not override it, override offset, no new lines
                {
                        "SELECT * FROM public.user ORDER BY created_at ASC LIMIT 1;",
                        3,
                        10,
                        true,
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
                        true,
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
                        true,
                        "SELECT name FROM product LIMIT 19 OFFSET 133;"
                },
                // limit and offset already used, offset before limit, override them all
                {
                        "SELECT name FROM product OFFSET 4 LIMIT 50;",
                        7,
                        19,
                        true,
                        "SELECT name FROM product OFFSET 133 LIMIT 19;"
                },
                // limit already used with value greater than allowed limit
                {
                        "SELECT name FROM product LIMIT 260",
                        null,
                        250,
                        false,
                        "SELECT name FROM product LIMIT 250;"
                }
        };
    }
}