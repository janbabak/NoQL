package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class PostgresDAOTest extends AbstractSqlDAOTest {

    public DatabaseDAO getDatabaseDao() {
        return postgresDAO;
    }

    /**
     * Get scripts for initialization of the databases
     */
    @Override
    protected InitScripts getInitializationScripts() {
        return InitScripts.postgres(
                FileUtils.getFileContent("./src/test/resources/dbInsertScripts/postgres/eshopUser.sql"));
    }


    @Test
    @DisplayName("Test create connection URL")
    void testCreateConnectionUrl() {
        String expected = "jdbc:postgresql://localhost:" + getPostgresPort() + "/test-database";
        assertEquals(expected, getDatabaseDao().createConnectionUrl());
    }
}