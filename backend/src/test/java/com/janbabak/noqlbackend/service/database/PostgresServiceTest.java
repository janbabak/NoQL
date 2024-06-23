package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.LocalDatabaseTest;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Schema;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Table;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Column;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.ForeignKey;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
class PostgresServiceTest extends LocalDatabaseTest {

    @SuppressWarnings("FieldCanBeLocal")
    private PostgresService postgresService;

    private SqlDatabaseStructure databaseStructure;
    public Database getDatabase() {
        return postgresDatabase;
    }

    private final String INTEGER_DATA_TYPE = "integer";
    @SuppressWarnings("FieldCanBeLocal")
    private final String NUMERIC_DATA_TYPE = "numeric";
    private final String VARCHAR_DATA_TYPE = "character varying";
    @SuppressWarnings("FieldCanBeLocal")
    private final String TEXT_DATA_TYPE = "text";
    @SuppressWarnings("FieldCanBeLocal")
    private final String CHAR_DATA_TYPE = "character";
    @SuppressWarnings("FieldCanBeLocal")
    private final String BOOLEAN_DATA_TYPE = "boolean";
    @SuppressWarnings("FieldCanBeLocal")
    private final String DATE_DATA_TYPE = "date";
    @SuppressWarnings("FieldCanBeLocal")
    private final String TIMESTAMP_DATA_TYPE = "timestamp without time zone";

    /**
     * Get scripts for initialization of the databases
     */
    @Override
    protected InitScripts getPostgresInitializationScripts() {
        return InitScripts.postgres(
                FileUtils.getFileContent("./src/test/resources/dbInsertScripts/postgresAllTables.sql"));
    }

    @BeforeAll
    @Override
    protected void setUp() throws DatabaseConnectionException, DatabaseExecutionException {
        super.setUp();
        postgresService = new PostgresService(getDatabase());
        databaseStructure = postgresService.retrieveSchema();
    }

    @Test
    @DisplayName("Test schemas")
    void testSchemas() {
        assertEquals(2, databaseStructure.getSchemas().size());
        assertTrue(databaseStructure.getSchemas().containsKey("public"));
        assertTrue(databaseStructure.getSchemas().containsKey("cvut"));
    }

    @Test
    @DisplayName("Test public schema")
    void testPublicSchema() {
        Schema schema = databaseStructure.getSchemas().get("public");
        assertEquals("public", schema.getName());
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
        Schema schema = databaseStructure.getSchemas().get("cvut");

        assertEquals("cvut", schema.getName());
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

    @Test
    @DisplayName("Test user table")
    void testUserTable() {
        Table table = databaseStructure.getSchemas().get("public").getTables().get("user");
        assertEquals(
                new Column("id", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("id"));
        assertEquals(
                new Column("name", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("name"));
        assertEquals(
                new Column("age", INTEGER_DATA_TYPE, false, null),
                table.getColumns().get("age"));
        assertEquals(
                new Column("sex", CHAR_DATA_TYPE, false, null),
                table.getColumns().get("sex"));
        assertEquals(
                new Column("email", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("email"));
        assertEquals(
                new Column("created_at", TIMESTAMP_DATA_TYPE, false, null),
                table.getColumns().get("created_at"));
    }

    @Test
    @DisplayName("Test address table")
    void testAddressTable() {
        Table table = databaseStructure.getSchemas().get("public").getTables().get("address");
        assertEquals(
                new Column("id", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("id"));
        assertEquals(
                new Column("user_id", INTEGER_DATA_TYPE, false,
                        new ForeignKey("public", "\"user\"", "id")),
                table.getColumns().get("user_id"));
        assertEquals(
                new Column("street", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("street"));
        assertEquals(
                new Column("city", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("city"));
        assertEquals(
                new Column("state", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("state"));
        assertEquals(
                new Column("postal_code", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("postal_code"));
    }

    @Test
    @DisplayName("Test order table")
    void testOrderTable() {
        Table table = databaseStructure.getSchemas().get("public").getTables().get("order");
        assertEquals(
                new Column("id", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("id"));
        assertEquals(
                new Column("user_id", INTEGER_DATA_TYPE, false,
                        new ForeignKey("public", "\"user\"", "id")),
                table.getColumns().get("user_id"));
        assertEquals(
                new Column("order_date", DATE_DATA_TYPE, false, null),
                table.getColumns().get("order_date"));
        assertEquals(
                new Column("total_amount", NUMERIC_DATA_TYPE, false, null),
                table.getColumns().get("total_amount"));
        assertEquals(
                new Column("payment_method", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("payment_method"));
        assertEquals(
                new Column("shipping_address_id", INTEGER_DATA_TYPE, false,
                        new ForeignKey("public", "address", "id")),
                table.getColumns().get("shipping_address_id"));
        assertEquals(
                new Column("is_shipped", BOOLEAN_DATA_TYPE, false, null),
                table.getColumns().get("is_shipped"));
        assertEquals(
                new Column("tracking_number", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("tracking_number"));
        assertEquals(
                new Column("status", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("status"));
        assertEquals(
                new Column("notes", TEXT_DATA_TYPE, false, null),
                table.getColumns().get("notes"));
    }

    @Test
    @DisplayName("Test specialisation table")
    void testSpecialisationTable() {
        Table table = databaseStructure.getSchemas().get("cvut").getTables().get("specialisation");
        assertEquals(
                new Column("id", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("id"));
        assertEquals(
                new Column("name", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("name"));
        assertEquals(
                new Column("manager", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("manager"));
    }

    @Test
    @DisplayName("Test student table")
    void testStudentTable() {
        Table table = databaseStructure.getSchemas().get("cvut").getTables().get("student");
        assertEquals(
                new Column("id", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("id"));
        assertEquals(
                new Column("name", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("name"));
        assertEquals(
                new Column("birthdate", DATE_DATA_TYPE, false, null),
                table.getColumns().get("birthdate"));
        assertEquals(
                new Column("grade", INTEGER_DATA_TYPE, false, null),
                table.getColumns().get("grade"));
        assertEquals(
                new Column("specialisation_id", INTEGER_DATA_TYPE, false,
                        new ForeignKey("cvut", "specialisation", "id")),
                table.getColumns().get("specialisation_id"));
    }

    @Test
    @DisplayName("Test fit_wiki table")
    void testFitWikiTable() {
        Table table = databaseStructure.getSchemas().get("cvut").getTables().get("fit_wiki");
        assertEquals(
                new Column("identifier", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("identifier"));
        assertEquals(
                new Column("data", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("data"));
        assertEquals(
                new Column("author", INTEGER_DATA_TYPE, false,
                        new ForeignKey("public", "\"user\"", "id")),
                table.getColumns().get("author"));
        assertEquals(
                new Column("reviewer_of_data", INTEGER_DATA_TYPE, false,
                        new ForeignKey("cvut", "student", "id")),
                table.getColumns().get("reviewer_of_data"));
    }

    @Test
    @DisplayName("Test course table")
    void testCourseTable() {
        Table table = databaseStructure.getSchemas().get("cvut").getTables().get("course");
        assertEquals(
                new Column("(identifier of course", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("(identifier of course"));
        assertEquals(
                new Column("name", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("name"));
    }

    @Test
    @DisplayName("Test exam table")
    void testExamTable() {
        Table table = databaseStructure.getSchemas().get("cvut").getTables().get("exam");
        assertEquals(
                new Column("student", INTEGER_DATA_TYPE, true,
                        new ForeignKey("cvut", "student", "id")),
                table.getColumns().get("student"));
        assertEquals(
                new Column("course", INTEGER_DATA_TYPE, true, new ForeignKey(
                        "cvut",
                        "course",
                        "\"(identifier of course\"")),
                table.getColumns().get("course"));
    }

    /**
     * Verify that the table contains the only expected columns and primary keys.
     *
     * @param table       table to verify
     * @param name        name of the table
     * @param primaryKeys primary keys of the table
     * @param columns     columns of the table
     */
    private void verifyTable(
            SqlDatabaseStructure.Table table,
            String name,
            List<String> primaryKeys,
            List<String> columns) {

        assertEquals(name, table.getName());
        assertEquals(columns.size(), table.getColumns().size());
        assertEquals(primaryKeys.size(), table.getPrimaryKeys().size());
        for (String column : columns) {
            assertTrue(table.getColumns().containsKey(column));
        }
        for (String primaryKey : primaryKeys) {
            assertTrue(table.getPrimaryKeys().contains(primaryKey));
        }
    }

}