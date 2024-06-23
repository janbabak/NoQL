package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.AbstractLocalDatabaseTest;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class MySqlServiceTest extends AbstractLocalDatabaseTest {
    @SuppressWarnings("FieldCanBeLocal")
    private MySqlService mySqlService;

    private SqlDatabaseStructure databaseStructure;

    private Database getDatabase() {
        return mySqlDatabase;
    }

    private final String INTEGER_DATA_TYPE = "int";
    @SuppressWarnings("FieldCanBeLocal")
    private final String NUMERIC_DATA_TYPE = "decimal";
    private final String VARCHAR_DATA_TYPE = "varchar";
    @SuppressWarnings("FieldCanBeLocal")
    private final String TEXT_DATA_TYPE = "text";
    @SuppressWarnings("FieldCanBeLocal")
    private final String CHAR_DATA_TYPE = "char";
    @SuppressWarnings("FieldCanBeLocal")
    private final String BOOLEAN_DATA_TYPE = "tinyint";
    @SuppressWarnings("FieldCanBeLocal")
    private final String DATE_DATA_TYPE = "date";
    @SuppressWarnings("FieldCanBeLocal")
    private final String TIMESTAMP_DATA_TYPE = "timestamp";

    /**
     * Get scripts for initialization of the databases
     */
    @Override
    protected InitScripts getInitializationScripts() {
        return InitScripts.mySql(
                FileUtils.getFileContent("./src/test/resources/dbInsertScripts/mySqlAllTables.sql"));
    }

    @BeforeAll
    @Override
    protected void setUp() throws DatabaseConnectionException, DatabaseExecutionException {
        super.setUp();
        mySqlService = new MySqlService(getDatabase());
        databaseStructure = mySqlService.retrieveSchema();
    }

    @Test
    @DisplayName("Test schemas")
    void testSchemas() {
        // MySQL does not have schemas like postgres. The top level is the database and it contains tables.
        assertEquals(1, databaseStructure.getSchemas().size());
        assertTrue(databaseStructure.getSchemas().containsKey(DATABASE_NAME));
    }

    @Test
    @DisplayName("Test default schema/database")
    void testDefaultSchema() {
        SqlDatabaseStructure.Schema schema = databaseStructure.getSchemas().get(DATABASE_NAME);
        assertEquals(DATABASE_NAME, schema.getName());
        assertEquals(8, schema.getTables().size());
        assertTrue(schema.getTables().containsKey("user"));
        assertTrue(schema.getTables().containsKey("address"));
        assertTrue(schema.getTables().containsKey("order"));
        assertTrue(schema.getTables().containsKey("specialisation"));
        assertTrue(schema.getTables().containsKey("student"));
        assertTrue(schema.getTables().containsKey("fit_wiki"));
        assertTrue(schema.getTables().containsKey("course"));
        assertTrue(schema.getTables().containsKey("exam"));

        verifyTable(schema.getTables().get("user"), "user", List.of("id"),
                List.of("id", "name", "age", "sex", "email", "created_at"));
        verifyTable(schema.getTables().get("address"), "address", List.of("id"),
                List.of("id", "user_id", "street", "city", "state", "postal_code"));
        verifyTable(schema.getTables().get("order"), "order", List.of("id"),
                List.of("id", "user_id", "order_date", "total_amount", "payment_method", "shipping_address_id",
                        "is_shipped", "tracking_number", "status", "notes"));
    }

    @Test
    @DisplayName("Test user table")
    void testUserTable() {
        SqlDatabaseStructure.Table table = databaseStructure.getSchemas().get(DATABASE_NAME).getTables().get("user");
        assertEquals(
                new SqlDatabaseStructure.Column("id", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("id"));
        assertEquals(
                new SqlDatabaseStructure.Column("name", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("name"));
        assertEquals(
                new SqlDatabaseStructure.Column("age", INTEGER_DATA_TYPE, false, null),
                table.getColumns().get("age"));
        assertEquals(
                new SqlDatabaseStructure.Column("sex", CHAR_DATA_TYPE, false, null),
                table.getColumns().get("sex"));
        assertEquals(
                new SqlDatabaseStructure.Column("email", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("email"));
        assertEquals(
                new SqlDatabaseStructure.Column("created_at", TIMESTAMP_DATA_TYPE, false, null),
                table.getColumns().get("created_at"));
    }

    @Test
    @DisplayName("Test address table")
    void testAddressTable() {
        SqlDatabaseStructure.Table table = databaseStructure.getSchemas().get(DATABASE_NAME).getTables().get("address");
        assertEquals(
                new SqlDatabaseStructure.Column("id", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("id"));
        assertEquals(
                new SqlDatabaseStructure.Column("user_id", INTEGER_DATA_TYPE, false,
                        new SqlDatabaseStructure.ForeignKey(DATABASE_NAME, "user", "id")),
                table.getColumns().get("user_id"));
        assertEquals(
                new SqlDatabaseStructure.Column("street", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("street"));
        assertEquals(
                new SqlDatabaseStructure.Column("city", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("city"));
        assertEquals(
                new SqlDatabaseStructure.Column("state", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("state"));
        assertEquals(
                new SqlDatabaseStructure.Column("postal_code", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("postal_code"));
    }

    @Test
    @DisplayName("Test order table")
    void testOrderTable() {
        SqlDatabaseStructure.Table table = databaseStructure.getSchemas().get(DATABASE_NAME).getTables().get("order");
        assertEquals(
                new SqlDatabaseStructure.Column("id", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("id"));
        assertEquals(
                new SqlDatabaseStructure.Column("user_id", INTEGER_DATA_TYPE, false,
                        new SqlDatabaseStructure.ForeignKey(DATABASE_NAME, "user", "id")),
                table.getColumns().get("user_id"));
        assertEquals(
                new SqlDatabaseStructure.Column("order_date", DATE_DATA_TYPE, false, null),
                table.getColumns().get("order_date"));
        assertEquals(
                new SqlDatabaseStructure.Column("total_amount", NUMERIC_DATA_TYPE, false, null),
                table.getColumns().get("total_amount"));
        assertEquals(
                new SqlDatabaseStructure.Column("payment_method", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("payment_method"));
        assertEquals(
                new SqlDatabaseStructure.Column("shipping_address_id", INTEGER_DATA_TYPE, false,
                        new SqlDatabaseStructure.ForeignKey(DATABASE_NAME, "address", "id")),
                table.getColumns().get("shipping_address_id"));
        assertEquals(
                new SqlDatabaseStructure.Column("is_shipped", BOOLEAN_DATA_TYPE, false, null),
                table.getColumns().get("is_shipped"));
        assertEquals(
                new SqlDatabaseStructure.Column("tracking_number", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("tracking_number"));
        assertEquals(
                new SqlDatabaseStructure.Column("status", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("status"));
        assertEquals(
                new SqlDatabaseStructure.Column("notes", TEXT_DATA_TYPE, false, null),
                table.getColumns().get("notes"));
    }

    @Test
    @DisplayName("Test specialisation table")
    void testSpecialisationTable() {
        SqlDatabaseStructure.Table table = databaseStructure.getSchemas().get(DATABASE_NAME).getTables().get("specialisation");
        assertEquals(
                new SqlDatabaseStructure.Column("id", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("id"));
        assertEquals(
                new SqlDatabaseStructure.Column("name", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("name"));
        assertEquals(
                new SqlDatabaseStructure.Column("manager", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("manager"));
    }

    @Test
    @DisplayName("Test student table")
    void testStudentTable() {
        SqlDatabaseStructure.Table table = databaseStructure.getSchemas().get(DATABASE_NAME).getTables().get("student");
        assertEquals(
                new SqlDatabaseStructure.Column("id", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("id"));
        assertEquals(
                new SqlDatabaseStructure.Column("name", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("name"));
        assertEquals(
                new SqlDatabaseStructure.Column("birthdate", DATE_DATA_TYPE, false, null),
                table.getColumns().get("birthdate"));
        assertEquals(
                new SqlDatabaseStructure.Column("grade", INTEGER_DATA_TYPE, false, null),
                table.getColumns().get("grade"));
        assertEquals(
                new SqlDatabaseStructure.Column("specialisation_id", INTEGER_DATA_TYPE, false,
                        new SqlDatabaseStructure.ForeignKey(DATABASE_NAME, "specialisation", "id")),
                table.getColumns().get("specialisation_id"));
    }

    @Test
    @DisplayName("Test fit_wiki table")
    void testFitWikiTable() {
        SqlDatabaseStructure.Table table = databaseStructure.getSchemas().get(DATABASE_NAME).getTables().get("fit_wiki");
        assertEquals(
                new SqlDatabaseStructure.Column("identifier", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("identifier"));
        assertEquals(
                new SqlDatabaseStructure.Column("data", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("data"));
        assertEquals(
                new SqlDatabaseStructure.Column("author", INTEGER_DATA_TYPE, false,
                        new SqlDatabaseStructure.ForeignKey(DATABASE_NAME, "user", "id")),
                table.getColumns().get("author"));
        assertEquals(
                new SqlDatabaseStructure.Column("reviewer_of_data", INTEGER_DATA_TYPE, false,
                        new SqlDatabaseStructure.ForeignKey(DATABASE_NAME, "student", "id")),
                table.getColumns().get("reviewer_of_data"));
    }

    @Test
    @DisplayName("Test course table")
    void testCourseTable() {
        SqlDatabaseStructure.Table table = databaseStructure.getSchemas().get(DATABASE_NAME).getTables().get("course");
        assertEquals(
                new SqlDatabaseStructure.Column("(identifier of course", INTEGER_DATA_TYPE, true, null),
                table.getColumns().get("(identifier of course"));
        assertEquals(
                new SqlDatabaseStructure.Column("name", VARCHAR_DATA_TYPE, false, null),
                table.getColumns().get("name"));
    }

    @Test
    @DisplayName("Test exam table")
    void testExamTable() {
        SqlDatabaseStructure.Table table = databaseStructure.getSchemas().get(DATABASE_NAME).getTables().get("exam");
        assertEquals(
                new SqlDatabaseStructure.Column("student", INTEGER_DATA_TYPE, true,
                        new SqlDatabaseStructure.ForeignKey(DATABASE_NAME, "student", "id")),
                table.getColumns().get("student"));
        assertEquals(
                new SqlDatabaseStructure.Column("course", INTEGER_DATA_TYPE, true, new SqlDatabaseStructure.ForeignKey(
                        DATABASE_NAME,
                        "course",
                        "(identifier of course")),
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