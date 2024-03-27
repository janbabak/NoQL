package com.janbabak.noqlbackend.model.database;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an SQL database object - contains information about schemas, tables, columns, primary keys, ...
 */
@Data
public class SqlDatabaseStructure implements DatabaseStructure {
    public final static String DEFAULT_SCHEMA = "public";
    private Map<String, Schema> schemas;

    /**
     * Create the database and automatically insert default public schema.
     */
    public SqlDatabaseStructure() {
        schemas = new HashMap<>();
        schemas.put(DEFAULT_SCHEMA, new Schema(DEFAULT_SCHEMA));
    }

    /**
     * Generates create script which can be help LLM understand the database schemas.
     *
     * @return insert script
     */
    public String generateCreateScript() {
        StringBuilder script = new StringBuilder();

        // schemas
        for (Map.Entry<String, Schema> schemaEntry : schemas.entrySet()) {
            script
                    .append("\nCREATE SCHEMA IF NOT EXISTS \"")
                    .append(schemaEntry.getValue().getName())
                    .append("\";\n");

            // tables
            for (Map.Entry<String, Table> tableEntry : schemaEntry.getValue().getTables().entrySet()) {
                script
                        .append("\nCREATE TABLE IF NOT EXISTS ")
                        .append(schemaEntry.getValue().getName())
                        .append(".")
                        .append(tableEntry.getValue().getName())
                        .append("\n(");

                List<String> primaryKeys = tableEntry.getValue().getPrimaryKeys();

                // columns
                for (Column column : tableEntry.getValue().getColumnsSortedByPrimaryKey()) {
                    script
                            .append("\n\t")
                            .append(column.getName())
                            .append(" ")
                            .append(column.getDataType().toUpperCase())
                            .append(primaryKeys.size() == 1 && column.getIsPrimaryKey()
                                    ? " PRIMARY KEY," // table has only 1 primary key and this column is the primary key
                                    : "")
                            .append(column.getForeignKey() != null
                                    ? column.getForeignKey().getReferencingString()
                                    : "");
                }

                script.append(primaryKeys.size() > 1 // table has multiple primary keys
                        ? "\n\tPRIMARY KEY (" + String.join(", ", primaryKeys) + ")\n);\n"
                        : "\n);\n");
            }
        }

        return script.toString().trim();
    }

    /**
     * Represents database schema.
     */
    @Data
    public static class Schema {
        private String name;
        private Map<String, Table> tables;

        public Schema(String name) {
            this.name = name;
            this.tables = new HashMap<>();
        }
    }

    /**
     * Represents database table inside a schema.
     */
    @Data
    public static class Table {
        private String name;
        private Map<String, Column> columns;

        public Table(String name) {

            this.name = name;
            this.columns = new HashMap<>();
        }

        /**
         * Get primary key(s) of the table
         *
         * @return names of columns
         */
        public List<String> getPrimaryKeys() {
            List<String> primaryKeys = new ArrayList<>();
            for (Map.Entry<String, Column> entry : columns.entrySet()) {
                if (entry.getValue().isPrimaryKey) {
                    primaryKeys.add(entry.getKey());
                }
            }
            return primaryKeys;
        }

        /**
         * Definitions usually starts by defining the primary key.
         *
         * @return columns sorted by {@code primaryKey} - primary keys are at the beginning.
         */
        @SuppressWarnings("all")
        public List<Column> getColumnsSortedByPrimaryKey() {
            return columns.values().stream().sorted((a, b) -> a.isPrimaryKey ? -1 : 1).toList();
        }
    }

    /**
     * Represents database column inside a table.
     */
    @Data
    public static class Column {
        private String name;
        private String dataType;
        private Boolean isPrimaryKey;
        private ForeignKey foreignKey; // if not null, this column references another column in another table

        public Column(String name, String dataType, Boolean isPrimaryKey) {
            this.name = name;
            this.dataType = dataType;
            this.isPrimaryKey = isPrimaryKey;
            this.foreignKey = null;
        }
    }

    /**
     * Foreign key / reference
     */
    @Data
    @AllArgsConstructor
    public static class ForeignKey {
        private String referencedSchema;
        private String referencedTable;
        private String referencedColumn;

        public String getReferencingString() {
            return " REFERENCES "
                    + (referencedSchema.equals(DEFAULT_SCHEMA) ? "" : referencedSchema + ".")
                    + referencedTable + "(" + referencedColumn + "),";
        }
    }
}
