package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Table;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class MySqlServiceTest extends AbstractSqlServiceTest {

    MySqlServiceTest() {
        super();

        INTEGER_DATA_TYPE = "int";
        NUMERIC_DATA_TYPE = "decimal";
        VARCHAR_DATA_TYPE = "varchar";
        TEXT_DATA_TYPE = "text";
        CHAR_DATA_TYPE = "char";
        BOOLEAN_DATA_TYPE = "tinyint";
        DATE_DATA_TYPE = "date";
        TIMESTAMP_DATA_TYPE = "timestamp";
    }

    protected Database getDatabase() {
        return mySqlDatabase;
    }

    protected SqlDatabaseService getSqlService(Database database) {
        return new MySqlService(database);
    }

    protected String getDefaultSchema() {
        return DATABASE_NAME;
    }

    protected String getCvutSchema() {
        return getDefaultSchema();
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
        return getTable(getDefaultSchema(), "specialisation");
    }

    protected Table getStudentTable() {
        return getTable(getDefaultSchema(), "student");
    }

    protected Table getFitWikiTable() {
        return getTable(getDefaultSchema(), "fit_wiki");
    }

    protected Table getCourseTable() {
        return getTable(getDefaultSchema(), "course");
    }

    protected Table getExamTable() {
        return getTable(getDefaultSchema(), "exam");
    }

    /**
     * Get scripts for initialization of the databases
     */
    @Override
    protected InitScripts getInitializationScripts() {
        return InitScripts.mySql(
                FileUtils.getFileContent("./src/test/resources/dbInsertScripts/mySql/allTables.sql"));
    }

    @Test
    @DisplayName("Test schemas")
    void testSchemas() {
        // MySQL does not have schemas like postgres. The top level is the database and it contains tables.
        assertEquals(1, databaseStructure.getSchemas().size());
        assertTrue(databaseStructure.getSchemas().containsKey(getDefaultSchema()));
    }

    @Test
    @DisplayName("Test default schema/database")
    void testDefaultSchema() {
        SqlDatabaseStructure.Schema schema = databaseStructure.getSchemas().get(getDefaultSchema());

        assertEquals(getDefaultSchema(), schema.getName());
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
}