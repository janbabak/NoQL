package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.entity.Database;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PostgresTest {

    @Autowired
    protected LocalPostgresService localPostgresService;

    @SuppressWarnings("FieldCanBeLocal")
    protected PostgresDAO postgresDAO;

    protected final Database postgresDatabase = Database.builder()
            .name("Local testing postgres")
            .host(LocalPostgresService.POSTGRES_HOST)
            .database(LocalPostgresService.POSTGRES_DB)
            .userName(LocalPostgresService.POSTGRES_USER)
            .password(LocalPostgresService.POSTGRES_PASSWORD)
            .port(LocalPostgresService.POSTGRES_PORT)
            .build();

    @BeforeAll
    protected void setUp() throws InterruptedException, DatabaseConnectionException, DatabaseExecutionException {
        localPostgresService.startPostgres();
        postgresDAO = new PostgresDAO(postgresDatabase);
        String createScript = getCreateScript();
        if (createScript != null) {
            postgresDAO.updateDatabase(createScript);
        }
    }

    @AfterAll
    void tearDown() {
        localPostgresService.stopPostgres();
    }

    protected abstract String getCreateScript();
}
