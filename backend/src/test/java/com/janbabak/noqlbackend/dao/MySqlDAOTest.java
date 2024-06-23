package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class MySqlDAOTest extends AbstractSqlDAOTest {

    public DatabaseDAO getDatabaseDao() {
        return mySqlDAO;
    }

    /**
     * Get scripts for initialization of the databases
     */
    @Override
    protected InitScripts getPostgresInitializationScripts() {
        return InitScripts.mySql(
                FileUtils.getFileContent("./src/test/resources/dbInsertScripts/mySql/eshopUser.sql"));
    }

    @Test
    @DisplayName("Test create connection URL")
    void testCreateConnectionUrl() {
        String expected = "jdbc:mysql://localhost:" + getMySqlPort() + "/test-database";
        assertEquals(expected, getDatabaseDao().createConnectionUrl());
    }
}