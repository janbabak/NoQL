package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class PostgresDAOTest extends AbstractSqlDAOTest {

    @Override
    public DatabaseDAO getDatabaseDao() {
        return postgresDAO;
    }

    /**
     * Get scripts for initialization of the databases
     */
    @Override
    protected Scripts getInitializationScripts() {
        return Scripts.postgres(
                FileUtils.getFileContent("./src/test/resources/dbScripts/postgres/eshopUser.sql"));
    }

    /**
     * Get scripts for cleanup of the databases.
     */
    @Override
    protected Scripts getCleanupScript() {
        return Scripts.postgres(
                FileUtils.getFileContent("./src/test/resources/dbScripts/postgres/eshopUserCleanup.sql"));
    }

    @Test
    @DisplayName("Test create connection URL")
    void testCreateConnectionUrl() {
        final String expected = "jdbc:postgresql://localhost:" + getPostgresPort() + "/test-database";
        assertEquals(expected, getDatabaseDao().createConnectionUrl());
    }
}