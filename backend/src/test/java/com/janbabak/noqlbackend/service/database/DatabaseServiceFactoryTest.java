package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.PostgresDAO;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Database;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseServiceFactoryTest {

    @Test
    @DisplayName("Test get database service with null database.")
    @SuppressWarnings("all")
    void testGetDatabaseServiceBadRequest() {
        assertThrows(NullPointerException.class, () -> DatabaseServiceFactory.getDatabaseService(null));
    }

    @ParameterizedTest
    @MethodSource("testGetDatabaseServiceDataProvider")
    @DisplayName("Test get database service based on the database engine.")
    void testGetDatabaseService(Database database, Class<? extends BaseDatabaseService> expected) {
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

    @Test
    @DisplayName("Test get database DAO with null database.")
    @SuppressWarnings("all")
    void testGetDatabaseDaoBadRequest() {
        assertThrows(NullPointerException.class, () -> DatabaseServiceFactory.getDatabaseDAO(null));
    }

    @ParameterizedTest
    @MethodSource("testGetDatabaseDaoDataProvider")
    @DisplayName("Test get database DAO based on the database engine.")
    void testGetDatabaseDao(Database database, Class<? extends DatabaseDAO> expected) {
        // when
        DatabaseDAO databaseDao = DatabaseServiceFactory.getDatabaseDAO(database);

        // then
        assertInstanceOf(expected, databaseDao);
    }

    static Object[][] testGetDatabaseDaoDataProvider() {
        return new Object[][]{
                {
                        Database.builder()
                                .name("postgres db")
                                .engine(DatabaseEngine.POSTGRES)
                                .build(),
                        PostgresDAO.class
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