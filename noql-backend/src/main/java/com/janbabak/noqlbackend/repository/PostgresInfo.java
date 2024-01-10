package com.janbabak.noqlbackend.repository;

import com.janbabak.noqlbackend.db.Database;
import org.antlr.v4.runtime.misc.Pair;

import java.sql.*;

public class PostgresInfo implements DatabaseInfo {

    private static final String TABLE_SCHEMA_COLUMN_NAME = "table_schema";
    private static final String TABLE_NAME_COLUMN_NAME = "table_name";
    private static final String COLUMN_NAME_COLUMN_NAME = "column_name";
    private static final String DATA_TYPE_COLUMN_NAME = "data_type";
    private static final String PRIMARY_KEY_COLUMN_NAME = "primary_key";
    private static final String FOREIGN_KEY_DEFINITION_COLUMN_NAME = "constraint_definition";

    public Database getSchema() throws SQLException {
        Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/postgres", "user", "password");
        Statement statement = connection.createStatement();
        String selectSchemasTablesColumnsPrimaryKeys = """
                SELECT columns.table_schema,
                       columns.table_name,
                       columns.column_name,
                       columns.data_type,
                       constraint_name IS NOT NULL AS primary_key
                FROM information_schema.columns AS columns
                LEFT JOIN information_schema.constraint_column_usage AS constrains
                    ON (columns.table_schema, columns.table_name, columns.column_name) =
                       (constrains.table_schema, constrains.table_name, constrains.column_name)
                    AND constrains.constraint_name LIKE '%pkey'
                WHERE columns.table_schema NOT LIKE 'pg_%'
                  AND columns.table_schema != 'information_schema'
                  AND columns.table_name IN (SELECT table_name
                                     FROM information_schema.tables
                                     WHERE table_type = 'BASE TABLE'
                                       AND table_catalog = current_database())
                ORDER BY table_schema, table_name, ordinal_position;
                """;

        ResultSet resultSet = statement.executeQuery(selectSchemasTablesColumnsPrimaryKeys);

        Database db = new Database();

        while (resultSet.next()) {
            String tableSchema = resultSet.getString(TABLE_SCHEMA_COLUMN_NAME);
            String tableName = resultSet.getString(TABLE_NAME_COLUMN_NAME);
            String columnName = resultSet.getString(COLUMN_NAME_COLUMN_NAME);
            String dataType = resultSet.getString(DATA_TYPE_COLUMN_NAME);
            Boolean primaryKey = resultSet.getBoolean(PRIMARY_KEY_COLUMN_NAME);

            Database.Schema schema = db.getSchemas().get(tableSchema);
            if (schema == null) {
                schema = new Database.Schema(tableSchema);
                db.getSchemas().put(tableSchema, schema);
            }

            Database.Table table = schema.getTables().get(tableName);
            if (table == null) {
                table = new Database.Table(tableName);
                schema.getTables().put(tableName, table);
            }

            // columnName key definitely doesn't exist - no need of checking it out
            table.getColumns().put(columnName, new Database.Column(columnName, dataType, primaryKey));
        }

        String getForeignKeys = """
                SELECT conrelid::regclass AS table_name,
                       conname AS foreign_key,
                       pg_get_constraintdef(oid) AS constraint_definition
                FROM   pg_constraint
                WHERE  contype = 'f'
                ORDER  BY conrelid::regclass::text, contype DESC;
                """;
        resultSet = statement.executeQuery(getForeignKeys);

        while (resultSet.next()) {
            String constraintDefinition = resultSet.getString(FOREIGN_KEY_DEFINITION_COLUMN_NAME);
            String referencingSchemaAndTable = resultSet.getString(TABLE_NAME_COLUMN_NAME);

            ForeignKeyData foreignKeyData = parseForeignKey(referencingSchemaAndTable, constraintDefinition);

            // insert that foreign key
            Database.Schema schema = db.getSchemas().get(foreignKeyData.referencingSchema);
            if (schema != null) {
                Database.Table table = schema.getTables().get(foreignKeyData.referencingTable);
                if (table != null) {
                    Database.Column column = table.getColumns().get(foreignKeyData.referencingColumn);
                    if (column != null) {
                        // TODO: Do I want to verify existence of these data?
                        column.setForeignKey(new Database.ForeignKey(
                                foreignKeyData.referencedSchema,
                                foreignKeyData.referencedTable,
                                foreignKeyData.referencedColumn
                        ));
                    }
                }
            }
        }

        return db;
    }

    public PostgresInfo() {
    }

    /**
     * Parse foreign key - extract all the necessary information like referencing and referenced schema, table, column
     * @param referencingSchemaAndTableName string that contains referencing schema and column
     * @param constraintDefinition definition of the constraint from the database in the following format:
     *                             {@code FOREIGN KEY (reviewer_of_data) REFERENCES cvut.student(id) }
     * @return foreign key data
     */
    private ForeignKeyData parseForeignKey(String referencingSchemaAndTableName, String constraintDefinition) {
        Pair<String, String> referencingSchemaAndTableParsed = parseSchemaAndTable(referencingSchemaAndTableName);
        String referencingSchema = referencingSchemaAndTableParsed.a;
        String referencingTable = referencingSchemaAndTableParsed.b;

        // removes the "FOREIGN KEY (" prefix
        String FOREIGN_KEY_SUBSTRING = "FOREIGN KEY (";
        constraintDefinition = constraintDefinition.substring(FOREIGN_KEY_SUBSTRING.length());

        String referencingColumn = "";
        String REFERENCES_SUBSTRING = ") REFERENCES ";
        int endOfReferencingColumnName = constraintDefinition.indexOf(REFERENCES_SUBSTRING);
        if (endOfReferencingColumnName != -1) {
            referencingColumn = constraintDefinition.substring(0, endOfReferencingColumnName);
            constraintDefinition = constraintDefinition.substring(
                    endOfReferencingColumnName + REFERENCES_SUBSTRING.length());
        }

        // if table or schema name contains "(" in its name, // doesn't work
        int leftBraceIndex = constraintDefinition.indexOf("(");
        String referencedSchemaAndTable = "";
        if (leftBraceIndex != -1) {
            referencedSchemaAndTable = constraintDefinition.substring(0, leftBraceIndex);
            // + 1 to remove the "(" character
            constraintDefinition = constraintDefinition.substring(leftBraceIndex + 1);
        }
        Pair<String, String> referencedSchemaAndTableParsed = parseSchemaAndTable(referencedSchemaAndTable);
        String referencedSchema = referencedSchemaAndTableParsed.a;
        String referencedTable = referencedSchemaAndTableParsed.b;

        // -1 to remove the ")" character
        String referencedColumn = constraintDefinition.substring(0, constraintDefinition.length() - 1);

        return new ForeignKeyData(
                referencingSchema,
                referencingTable,
                referencingColumn,
                referencedSchema,
                referencedTable,
                referencedColumn);
    }

    /**
     * Heuristic responsible for parsing schema and table string.
     * If the database contains dot in schema or table names, may not work properly.
     * @param data schema and table name together
     * @return [schemaName, tableName]
     */
    private Pair<String, String> parseSchemaAndTable(String data) {
        int dotIndex = data.indexOf(".");

        // when dot is not found, table comes from the default (public) schema
        if (dotIndex == -1) {
            return new Pair<>(Database.DEFAULT_SCHEMA, data);
        }

        // when dot is found, it usually separates the schema and table name (unless the schema name contains dot)
        return new Pair<>(
                data.substring(0, dotIndex), // schema
                data.substring(dotIndex + 1) // table
        );
    }

    private record ForeignKeyData(
            String referencingSchema,
            String referencingTable,
            String referencingColumn,
            String referencedSchema,
            String referencedTable,
            String referencedColumn
    ) {}
}
