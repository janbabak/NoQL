package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Schema;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Table;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
class PostgresServiceTest extends AbstractSqlServiceTest {

    @Autowired
    private PostgresService postgresService;

    PostgresServiceTest() {
        super();

        INTEGER_DATA_TYPE = "integer";
        NUMERIC_DATA_TYPE = "numeric";
        VARCHAR_DATA_TYPE = "character varying";
        TEXT_DATA_TYPE = "text";
        CHAR_DATA_TYPE = "character";
        BOOLEAN_DATA_TYPE = "boolean";
        DATE_DATA_TYPE = "date";
        TIMESTAMP_DATA_TYPE = "timestamp without time zone";
    }

    protected Database getDatabase() {
        return postgresDatabase;
    }

    protected SqlDatabaseService getSqlService(Database database) {
        postgresService.setDatabaseDaoMetadata(database);
        return postgresService;
    }

    protected String getDefaultSchema() {
        return "public";
    }

    protected String getCvutSchema() {
        return "cvut";
    }

    protected Table getUserTable() {
        return getTable(getDefaultSchema(), "user");
    }

    protected Table getAddressTable() {
        return getTable(getDefaultSchema(), "address");
    }

    protected Table getOrderTable() {
        return getTable(getDefaultSchema(), "order");
    }

    protected Table getSpecialisationTable() {
        return getTable(getCvutSchema(), "specialisation");
    }

    protected Table getStudentTable() {
        return getTable(getCvutSchema(), "student");
    }

    protected Table getFitWikiTable() {
        return getTable(getCvutSchema(), "fit_wiki");
    }

    protected Table getCourseTable() {
        return getTable(getCvutSchema(), "course");
    }

    protected Table getExamTable() {
        return getTable(getCvutSchema(), "exam");
    }

    @Override
    protected String getUserTableName() {
        return "\"user\"";
    }

    @Override
    protected String getCourseIdentifier() {
        return "\"(identifier of course\"";
    }

    /**
     * Get scripts for initialization of the databases
     */
    @Override
    protected Scripts getInitializationScripts() {
        return Scripts.postgres(
                FileUtils.getFileContent("./src/test/resources/dbScripts/postgres/allTables.sql"));
    }

    /**
     * Get scripts for cleanup of the databases.
     */
    @Override
    protected Scripts getCleanupScript() {
        return Scripts.postgres(
                FileUtils.getFileContent("./src/test/resources/dbScripts/postgres/allTablesCleanup.sql"));
    }

    @Test
    @DisplayName("Test schemas")
    void testSchemas() {
        assertEquals(2, databaseStructure.schemas().size());
        assertTrue(databaseStructure.schemas().containsKey(getDefaultSchema()));
        assertTrue(databaseStructure.schemas().containsKey(getCvutSchema()));
    }

    @Test
    @DisplayName("Test public schema")
    void testPublicSchema() {
        Schema schema = databaseStructure.schemas().get(getDefaultSchema());

        assertEquals(getDefaultSchema(), schema.name());
        assertEquals(3, schema.tables().size());
        assertTrue(schema.tables().containsKey("user"));
        assertTrue(schema.tables().containsKey("address"));
        assertTrue(schema.tables().containsKey("order"));

        verifyTable(schema.tables().get("user"), "user", List.of("id"),
                List.of("id", "name", "age", "sex", "email", "created_at"));

        verifyTable(schema.tables().get("address"), "address", List.of("id"),
                List.of("id", "user_id", "street", "city", "state", "postal_code"));

        verifyTable(schema.tables().get("order"), "order", List.of("id"),
                List.of("id", "user_id", "order_date", "total_amount", "payment_method", "shipping_address_id",
                        "is_shipped", "tracking_number", "status", "notes"));
    }

    @Test
    @DisplayName("Test cvut schema")
    void testCvutSchema() {
        Schema schema = databaseStructure.schemas().get(getCvutSchema());

        assertEquals(getCvutSchema(), schema.name());
        assertEquals(5, schema.tables().size());
        assertTrue(schema.tables().containsKey("specialisation"));
        assertTrue(schema.tables().containsKey("student"));
        assertTrue(schema.tables().containsKey("fit_wiki"));
        assertTrue(schema.tables().containsKey("course"));
        assertTrue(schema.tables().containsKey("exam"));

        verifyTable(schema.tables().get("specialisation"), "specialisation", List.of("id"),
                List.of("id", "name", "manager"));

        verifyTable(schema.tables().get("student"), "student", List.of("id"),
                List.of("id", "name", "birthdate", "grade", "specialisation_id"));

        verifyTable(schema.tables().get("fit_wiki"), "fit_wiki", List.of("identifier"),
                List.of("identifier", "data", "author", "reviewer_of_data"));

        verifyTable(schema.tables().get("course"), "course", List.of("(identifier of course"),
                List.of("(identifier of course", "name"));

        verifyTable(schema.tables().get("exam"), "exam", List.of("student", "course"),
                List.of("student", "course"));
    }
}