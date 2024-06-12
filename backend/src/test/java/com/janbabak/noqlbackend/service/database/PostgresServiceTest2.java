package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.PostgresDAO;
import com.janbabak.noqlbackend.model.entity.Database;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Testcontainers
public class PostgresServiceTest2 {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test-database")
            .withUsername("test-user")
            .withPassword("test-password");

    protected final Database postgresDatabase;

    PostgresServiceTest2() {
        postgresDatabase = Database.builder()
                .name("Local testing postgres")
                .host(postgres.getHost())
                .database(postgres.getDatabaseName())
                .userName(postgres.getUsername())
                .password(postgres.getPassword())
                .port(postgres.getFirstMappedPort())
                .build();
        postgresDAO = new PostgresDAO(postgresDatabase);
    }

    static PostgresDAO postgresDAO;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.name", postgres::getDatabaseName);
    }

    @Test
    @DisplayName("Test connection")
    void testConnection() {
        assertDoesNotThrow(() -> postgresDAO.testConnection());
    }

    @Test
    void someTest() {
        // Your test code here
    }
}
