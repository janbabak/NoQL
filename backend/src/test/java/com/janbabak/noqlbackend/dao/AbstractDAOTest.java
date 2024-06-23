package com.janbabak.noqlbackend.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public abstract class AbstractDAOTest extends AbstractLocalDatabaseTest {

    @Test
    @DisplayName("Test connection")
    void testConnection() {
        assertDoesNotThrow(() -> getDatabaseDao().testConnection());
    }

    abstract DatabaseDAO getDatabaseDao();
}
