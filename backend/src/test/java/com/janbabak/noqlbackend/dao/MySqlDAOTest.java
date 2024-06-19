package com.janbabak.noqlbackend.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest
class MySqlDAOTest extends MySqlTest {

    @Override
    protected String getCreateScript() {
        return null; // TODO: implement
    }

    @Test
    @DisplayName("Test create connection URL")
    void testCreateConnectionUrl() {
        String expected = "jdbc:mysql://localhost:" + getDatabasePort() + "/mysql-test-database";
        assertEquals(expected, mysqlDAO.createConnectionUrl());
    }
}