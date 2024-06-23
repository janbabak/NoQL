package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Schema;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Table;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
class PostgresServiceTest extends AbstractSqlServiceTest {

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
        return new PostgresService(database);
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
    protected InitScripts getInitializationScripts() {
        return InitScripts.postgres(
                FileUtils.getFileContent("./src/test/resources/dbInsertScripts/postgres/allTables.sql"));
    }

    @Test
    @DisplayName("Test schemas")
    void testSchemas() {
        assertEquals(2, databaseStructure.getSchemas().size());
        assertTrue(databaseStructure.getSchemas().containsKey(getDefaultSchema()));
        assertTrue(databaseStructure.getSchemas().containsKey(getCvutSchema()));
    }

    @Test
    @DisplayName("Test public schema")
    void testPublicSchema() {
        Schema schema = databaseStructure.getSchemas().get(getDefaultSchema());

        assertEquals(getDefaultSchema(), schema.getName());
        assertEquals(3, schema.getTables().size());
        assertTrue(schema.getTables().containsKey("user"));
        assertTrue(schema.getTables().containsKey("address"));
        assertTrue(schema.getTables().containsKey("order"));

        verifyTable(schema.getTables().get("user"), "user", List.of("id"),
                List.of("id", "name", "age", "sex", "email", "created_at"));

        verifyTable(schema.getTables().get("address"), "address", List.of("id"),
                List.of("id", "user_id", "street", "city", "state", "postal_code"));

        verifyTable(schema.getTables().get("order"), "order", List.of("id"),
                List.of("id", "user_id", "order_date", "total_amount", "payment_method", "shipping_address_id",
                        "is_shipped", "tracking_number", "status", "notes"));
    }

    @Test
    @DisplayName("Test cvut schema")
    void testCvutSchema() {
        Schema schema = databaseStructure.getSchemas().get(getCvutSchema());

        assertEquals(getCvutSchema(), schema.getName());
        assertEquals(5, schema.getTables().size());
        assertTrue(schema.getTables().containsKey("specialisation"));
        assertTrue(schema.getTables().containsKey("student"));
        assertTrue(schema.getTables().containsKey("fit_wiki"));
        assertTrue(schema.getTables().containsKey("course"));
        assertTrue(schema.getTables().containsKey("exam"));

        verifyTable(schema.getTables().get("specialisation"), "specialisation", List.of("id"),
                List.of("id", "name", "manager"));

        verifyTable(schema.getTables().get("student"), "student", List.of("id"),
                List.of("id", "name", "birthdate", "grade", "specialisation_id"));

        verifyTable(schema.getTables().get("fit_wiki"), "fit_wiki", List.of("identifier"),
                List.of("identifier", "data", "author", "reviewer_of_data"));

        verifyTable(schema.getTables().get("course"), "course", List.of("(identifier of course"),
                List.of("(identifier of course", "name"));

        verifyTable(schema.getTables().get("exam"), "exam", List.of("student", "course"),
                List.of("student", "course"));
    }
}