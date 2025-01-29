package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.MySqlDAO;
import com.janbabak.noqlbackend.dao.PostgresDAO;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Database;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseServiceFactoryTest {

    @Autowired
    private DatabaseServiceFactory databaseServiceFactory;

    @Test
    @DisplayName("Test get database service with null database.")
    @SuppressWarnings("all")
    void testGetDatabaseServiceBadRequest() {
        assertThrows(NullPointerException.class, () -> databaseServiceFactory.getDatabaseService(null));
    }

    @ParameterizedTest
    @MethodSource("testGetDatabaseServiceDataProvider")
    @DisplayName("Test get database service based on the database engine.")
    void testGetDatabaseService(Database database, Class<? extends BaseDatabaseService> expected) {
        assertInstanceOf(expected, databaseServiceFactory.getDatabaseService(database));
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
                {
                        Database.builder()
                                .name("my-sql db")
                                .engine(DatabaseEngine.MYSQL)
                                .build(),
                        MySqlService.class
                }
        };
    }

    @Test
    @DisplayName("Test get database DAO with null database.")
    @SuppressWarnings("all")
    void testGetDatabaseDaoBadRequest() {
        assertThrows(NullPointerException.class, () -> databaseServiceFactory.getDatabaseDAO(null));
    }

    @ParameterizedTest
    @MethodSource("testGetDatabaseDaoDataProvider")
    @DisplayName("Test get database DAO based on the database engine.")
    void testGetDatabaseDao(Database database, Class<? extends DatabaseDAO> expected) {
        assertInstanceOf(expected, databaseServiceFactory.getDatabaseDAO(database));
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
                {
                        Database.builder()
                                .name("my-sql db")
                                .engine(DatabaseEngine.MYSQL)
                                .build(),
                        MySqlDAO.class
                }
        };
    }
}