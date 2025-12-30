package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Table;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.utils.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class MySqlServiceTest extends AbstractSqlServiceTest {

    @Autowired
    private MySqlService mySqlService;

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

    @Override
    protected Database getDatabase() {
        return mySqlDatabase;
    }

    @Override
    protected SqlDatabaseService getSqlService(Database database) {
        return mySqlService;
    }

    @Override
    protected String getDefaultSchema() {
        return DATABASE_NAME;
    }

    @Override
    protected String getCvutSchema() {
        return getDefaultSchema();
    }

    @Override
    protected Table getUserTable() {
        return getTable(getDefaultSchema(), "user");
    }

    @Override
    protected Table getAddressTable() {
        return getTable(getDefaultSchema(), "address");
    }

    @Override
    protected Table getOrderTable() {
        return getTable(getDefaultSchema(), "order");
    }

    @Override
    protected Table getSpecialisationTable() {
        return getTable(getDefaultSchema(), "specialisation");
    }

    @Override
    protected Table getStudentTable() {
        return getTable(getDefaultSchema(), "student");
    }

    @Override
    protected Table getFitWikiTable() {
        return getTable(getDefaultSchema(), "fit_wiki");
    }

    @Override
    protected Table getCourseTable() {
        return getTable(getDefaultSchema(), "course");
    }

    @Override
    protected Table getExamTable() {
        return getTable(getDefaultSchema(), "exam");
    }

    /**
     * Get scripts for initialization of the databases
     */
    @Override
    protected Scripts getInitializationScripts() {
        return Scripts.mySql(
                FileUtils.getFileContent("./src/test/resources/dbScripts/mySql/allTables.sql"));
    }

    /**
     * Get scripts for cleanup of the databases.
     */
    @Override
    protected Scripts getCleanupScript() {
        return Scripts.mySql(
                FileUtils.getFileContent("./src/test/resources/dbScripts/mySql/allTablesCleanup.sql"));
    }

    @Test
    @DisplayName("Test schemas")
    void testSchemas() {
        // MySQL does not have schemas like postgres. The top level is the database and it contains tables.
        assertEquals(1, databaseStructure.schemas().size());
        assertTrue(databaseStructure.schemas().containsKey(getDefaultSchema()));
    }

    @Test
    @DisplayName("Test default schema/database")
    void testDefaultSchema() {
        final SqlDatabaseStructure.Schema schema = databaseStructure.schemas().get(getDefaultSchema());

        assertEquals(getDefaultSchema(), schema.name());
        assertEquals(8, schema.tables().size());
        assertTrue(schema.tables().containsKey("user"));
        assertTrue(schema.tables().containsKey("address"));
        assertTrue(schema.tables().containsKey("order"));
        assertTrue(schema.tables().containsKey("specialisation"));
        assertTrue(schema.tables().containsKey("student"));
        assertTrue(schema.tables().containsKey("fit_wiki"));
        assertTrue(schema.tables().containsKey("course"));
        assertTrue(schema.tables().containsKey("exam"));

        verifyTable(schema.tables().get("user"), "user", List.of("id"),
                List.of("id", "name", "age", "sex", "email", "created_at"));

        verifyTable(schema.tables().get("address"), "address", List.of("id"),
                List.of("id", "user_id", "street", "city", "state", "postal_code"));

        verifyTable(schema.tables().get("order"), "order", List.of("id"),
                List.of("id", "user_id", "order_date", "total_amount", "payment_method", "shipping_address_id",
                        "is_shipped", "tracking_number", "status", "notes"));
    }
}