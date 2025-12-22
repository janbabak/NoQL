package com.janbabak.noqlbackend.service.query;

import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Database;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import static com.janbabak.noqlbackend.service.query.QueryUtils.setPaginationInSqlQuery;
import static com.janbabak.noqlbackend.service.query.QueryUtils.trimAndRemoveTrailingSemicolon;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class QueryUtilsTest {

    @ParameterizedTest
    @MethodSource("setPaginationDataProvider")
    @DisplayName("Test set pagination")
    void testSetPagination(String query, Integer page, Integer pageSize, QueryUtils.PaginatedQuery expectedQuery)
            throws BadRequestException {

        try (MockedStatic<Settings> settingsMockedStatic = mockStatic(Settings.class)) {
            // given
            settingsMockedStatic.when(Settings::getMaxPageSizeStatic).thenReturn(50);
            if (pageSize == null) {
                settingsMockedStatic.when(Settings::getDefaultPageSizeStatic).thenReturn(10);
            }

            Database database = Database.builder().engine(DatabaseEngine.POSTGRES).build();

            // when
            QueryUtils.PaginatedQuery actualValue = setPaginationInSqlQuery(query, page, pageSize, database);

            // then
            assertEquals(expectedQuery, actualValue);
        }
    }

    @SuppressWarnings("all") // sql warnings
    static Object[][] setPaginationDataProvider() {
        return new Object[][]{
                // trailing semicolon
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        8,
                        15,
                        new QueryUtils.PaginatedQuery(
                                // language=SQL
                                "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) AS query LIMIT 15 OFFSET 120;",
                                8,
                                15)
                },
                // no trailing semicolon
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3",
                        8,
                        15,
                        new QueryUtils.PaginatedQuery(
                                // language=SQL
                                "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) AS query LIMIT 15 OFFSET 120;",
                                8,
                                15)
                },
                // null page and page size
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        null,
                        null,
                        new QueryUtils.PaginatedQuery(
                                // language=SQL
                                "SELECT * FROM (SELECT name FROM cvut.student WHERE grade < 3) AS query LIMIT 10 OFFSET 0;",
                                0,
                                10)
                },
                // null page size
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        4,
                        null, // null page size
                        new QueryUtils.PaginatedQuery(
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
        try (MockedStatic<Settings> settingsMockedStatic = mockStatic(Settings.class)) {
            // given
            if (page >= 0) { // otherwise unnecessary stubbing error
                settingsMockedStatic.when(Settings::getMaxPageSizeStatic).thenReturn(50);
            }
            Database database = Database.builder().engine(DatabaseEngine.POSTGRES).build();


            // when
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> setPaginationInSqlQuery(query, page, pageSize, database));

            // then
            assertEquals(errorMessage, exception.getMessage());
        }
    }

    @SuppressWarnings("all") // sql warnings
    static Object[][] setPaginationBadRequestDataProvider() {
        return new Object[][]{
                // page size greater than maximum allowed
                {
                        // language=SQL
                        "SELECT name FROM cvut.student WHERE grade < 3;",
                        5,
                        60,
                        "Page size is greater than maximum allowed value=50"
                },
                // negative page number
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
        String actualValue = trimAndRemoveTrailingSemicolon(query);

        // then
        assertEquals(expectedQuery, actualValue);
    }

    @SuppressWarnings("all") // sql warnings
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
}
