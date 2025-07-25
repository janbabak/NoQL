package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.LocalDatabaseTest;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Table;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Column;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.ForeignKey;
import com.janbabak.noqlbackend.model.entity.Database;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractSqlServiceTest extends LocalDatabaseTest {

    // these data type fields must be set in the constructor of the extending class
    protected String INTEGER_DATA_TYPE;
    protected String NUMERIC_DATA_TYPE;
    protected String VARCHAR_DATA_TYPE;
    protected String TEXT_DATA_TYPE;
    protected String CHAR_DATA_TYPE;
    protected String BOOLEAN_DATA_TYPE;
    protected String DATE_DATA_TYPE;
    protected String TIMESTAMP_DATA_TYPE;

    protected SqlDatabaseStructure databaseStructure;

    abstract Database getDatabase();
    abstract SqlDatabaseService getSqlService(Database database);
    abstract String getDefaultSchema();
    abstract String getCvutSchema();
    abstract Table getUserTable();
    abstract Table getAddressTable();
    abstract Table getOrderTable();
    abstract Table getSpecialisationTable();
    abstract Table getStudentTable();
    abstract Table getFitWikiTable();
    abstract Table getCourseTable();
    abstract Table getExamTable();

    protected Table getTable(String schema, String tableName) {
        return databaseStructure.schemas().get(schema).tables().get(tableName);
    }

    protected String getUserTableName() {
        return "user";
    }

    protected String getCourseIdentifier() {
        return "(identifier of course";
    }

    /**
     * Verify that the table contains the only expected columns and primary keys.
     *
     * @param table       table to verify
     * @param name        name of the table
     * @param primaryKeys primary keys of the table
     * @param columns     columns of the table
     */
    protected void verifyTable(Table table, String name, List<String> primaryKeys, List<String> columns) {
        assertEquals(name, table.name());
        assertEquals(columns.size(), table.columns().size());
        assertEquals(primaryKeys.size(), table.getPrimaryKeys().size());
        for (String column : columns) {
            assertTrue(table.columns().containsKey(column));
        }
        for (String primaryKey : primaryKeys) {
            assertTrue(table.getPrimaryKeys().contains(primaryKey));
        }
    }

    @BeforeAll
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SqlDatabaseService databaseService = getSqlService(getDatabase());
        databaseService.setDatabaseDaoMetadata(getDatabase());
        databaseStructure = databaseService.retrieveSchema();
    }

    @Test
    @DisplayName("Test user table")
    void testUserTable() {
        Table table = getUserTable();

        assertEquals(new Column("id", INTEGER_DATA_TYPE, true, null),
                table.columns().get("id"));

        assertEquals(new Column("name", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("name"));

        assertEquals(new Column("age", INTEGER_DATA_TYPE, false, null),
                table.columns().get("age"));

        assertEquals(new Column("sex", CHAR_DATA_TYPE, false, null),
                table.columns().get("sex"));

        assertEquals(new Column("email", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("email"));

        assertEquals(new Column("created_at", TIMESTAMP_DATA_TYPE, false, null),
                table.columns().get("created_at"));
    }

    @Test
    @DisplayName("Test address table")
    void testAddressTable() {
        Table table = getAddressTable();

        assertEquals(new Column("id", INTEGER_DATA_TYPE, true, null),
                table.columns().get("id"));

        assertEquals(new Column("user_id", INTEGER_DATA_TYPE, false,
                        new ForeignKey(getDefaultSchema(), getUserTableName(), "id")),
                table.columns().get("user_id"));

        assertEquals(new Column("street", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("street"));

        assertEquals(new Column("city", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("city"));

        assertEquals(new Column("state", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("state"));

        assertEquals(new Column("postal_code", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("postal_code"));
    }

    @Test
    @DisplayName("Test order table")
    void testOrderTable() {
        Table table = getOrderTable();

        assertEquals(new Column("id", INTEGER_DATA_TYPE, true, null),
                table.columns().get("id"));

        assertEquals(new Column("user_id", INTEGER_DATA_TYPE, false,
                        new ForeignKey(getDefaultSchema(), getUserTableName(), "id")),
                table.columns().get("user_id"));

        assertEquals(new Column("order_date", DATE_DATA_TYPE, false, null),
                table.columns().get("order_date"));

        assertEquals(new Column("total_amount", NUMERIC_DATA_TYPE, false, null),
                table.columns().get("total_amount"));

        assertEquals(new Column("payment_method", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("payment_method"));

        assertEquals(new Column("shipping_address_id", INTEGER_DATA_TYPE, false,
                        new ForeignKey(getDefaultSchema(), "address", "id")),
                table.columns().get("shipping_address_id"));

        assertEquals(new Column("is_shipped", BOOLEAN_DATA_TYPE, false, null),
                table.columns().get("is_shipped"));

        assertEquals(new Column("tracking_number", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("tracking_number"));

        assertEquals(new Column("status", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("status"));

        assertEquals(new Column("notes", TEXT_DATA_TYPE, false, null),
                table.columns().get("notes"));
    }

    @Test
    @DisplayName("Test specialisation table")
    void testSpecialisationTable() {
        Table table = getSpecialisationTable();

        assertEquals(new Column("id", INTEGER_DATA_TYPE, true, null),
                table.columns().get("id"));

        assertEquals(new Column("name", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("name"));

        assertEquals(new Column("manager", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("manager"));
    }

    @Test
    @DisplayName("Test student table")
    void testStudentTable() {
        Table table = getStudentTable();

        assertEquals(new Column("id", INTEGER_DATA_TYPE, true, null),
                table.columns().get("id"));

        assertEquals(new Column("name", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("name"));

        assertEquals(new Column("birthdate", DATE_DATA_TYPE, false, null),
                table.columns().get("birthdate"));

        assertEquals(new Column("grade", INTEGER_DATA_TYPE, false, null),
                table.columns().get("grade"));

        assertEquals(new Column("specialisation_id", INTEGER_DATA_TYPE, false,
                        new ForeignKey(getCvutSchema(), "specialisation", "id")),
                table.columns().get("specialisation_id"));
    }

    @Test
    @DisplayName("Test fit_wiki table")
    void testFitWikiTable() {
        Table table = getFitWikiTable();

        assertEquals(new Column("identifier", INTEGER_DATA_TYPE, true, null),
                table.columns().get("identifier"));

        assertEquals(new Column("data", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("data"));

        assertEquals(new Column("author", INTEGER_DATA_TYPE, false,
                        new ForeignKey(getDefaultSchema(), getUserTableName(), "id")),
                table.columns().get("author"));

        assertEquals(new Column("reviewer_of_data", INTEGER_DATA_TYPE, false,
                        new ForeignKey(getCvutSchema(), "student", "id")),
                table.columns().get("reviewer_of_data"));
    }

    @Test
    @DisplayName("Test course table")
    void testCourseTable() {
        Table table = getCourseTable();

        assertEquals(new Column("(identifier of course", INTEGER_DATA_TYPE, true, null),
                table.columns().get("(identifier of course"));
        assertEquals(
                new Column("name", VARCHAR_DATA_TYPE, false, null),
                table.columns().get("name"));
    }

    @Test
    @DisplayName("Test exam table")
    void testExamTable() {
        Table table = getExamTable();

        assertEquals(new Column("student", INTEGER_DATA_TYPE, true,
                        new ForeignKey(getCvutSchema(), "student", "id")),
                table.columns().get("student"));

        assertEquals(new Column("course", INTEGER_DATA_TYPE, true,
                        new ForeignKey(getCvutSchema(), "course", getCourseIdentifier())),
                table.columns().get("course"));
    }
}
