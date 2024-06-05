package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Database;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseServiceFactoryTest {

    @ParameterizedTest
    @MethodSource("testGetDatabaseServiceDataProvider")
    @DisplayName("Test get database service based on the database engine.")
    void testGetDatabaseService(Database database, Class<?> expected) {
        // when
        BaseDatabaseService databaseService = DatabaseServiceFactory.getDatabaseService(database);

        // then
        assertInstanceOf(expected, databaseService);
    }

    static Object[][] testGetDatabaseServiceDataProvider() {
        return new Object[][]{
                {
                        Database.builder()
                                .name("postgres db")
                                .engine(DatabaseEngine.POSTGRES)
                                .build(),
                        PostgresService.class
                },
                // TODO: implement database service for MySQL
               /* {
                        Database.builder()
                                .name("postgres db")
                                .engine(DatabaseEngine.MYSQL)
                                .build(),
                        null
                }*/

        };
    }
}