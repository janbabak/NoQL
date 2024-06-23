package com.janbabak.noqlbackend.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ActiveProfiles("test")
@SpringBootTest
public abstract class AbstractDAOTest extends LocalDatabaseTest {

    @Test
    @DisplayName("Test connection")
    void testConnection() {
        assertDoesNotThrow(() -> getDatabaseDao().testConnection());
    }

    abstract DatabaseDAO getDatabaseDao();
}
