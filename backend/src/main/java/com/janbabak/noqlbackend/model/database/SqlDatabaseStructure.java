package com.janbabak.noqlbackend.model.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an SQL database object - contains information about schemas, tables, columns, primary keys
 */
public record SqlDatabaseStructure(Map<String, Schema> schemas) implements DatabaseStructure {
    public static final String DEFAULT_SCHEMA = "public";

    /**
     * Create the database and automatically insert default public schema.
     */
    public SqlDatabaseStructure() {
        this(new HashMap<>());
    }

    /**
     * Generates create script which can be help LLM understand the database schemas.
     *
     * @return insert script
     */
    @Override
    public String generateCreateScript() {
        final StringBuilder script = new StringBuilder();

        // schemas
        schemas().forEach((schemaName, schema) -> {
            script
                    .append("\nCREATE SCHEMA IF NOT EXISTS \"")
                    .append(schemaName)
                    .append("\";\n");

            // tables
            schema.tables.forEach((tableName, table) -> {
                script
                        .append("\nCREATE TABLE IF NOT EXISTS ")
                        .append(schemaName)
                        .append(".")
                        .append(tableName)
                        .append("\n(");

                final List<String> primaryKeys = table.getPrimaryKeys();

                // columns
                table.getColumnsSortedByPrimaryKey().forEach(column -> script
                        .append("\n\t")
                        .append(column.getName())
                        .append(" ")
                        .append(column.getDataType().toUpperCase())
                        .append(primaryKeys.size() == 1 && column.getIsPrimaryKey()
                                ? " PRIMARY KEY," // table has only 1 primary key and this column is the primary key
                                : "")
                        .append(column.getForeignKey() != null
                                ? column.getForeignKey().getReferencingString()
                                : ""));

                script.append(primaryKeys.size() > 1 // table has multiple primary keys
                        ? "\n\tPRIMARY KEY (" + String.join(", ", primaryKeys) + ")\n);\n"
                        : "\n);\n");
            });
        });

        return script.toString().trim();
    }

    /**
     * Get data transfer object
     *
     * @return DTO
     */
    @Override
    public SqlDatabaseStructureDto toDto() {
        return new SqlDatabaseStructureDto(this);
    }

    /**
     * Represents database schema.
     */
    public record Schema(
            String name,
            Map<String, Table> tables
    ) {
        public Schema(String name) {
            this(name, new HashMap<>());
        }
    }

    /**
     * Represents database table inside a schema.
     */
    public record Table(
            String name,
            Map<String, Column> columns
    ) {
        public Table(String name) {
            this(name, new HashMap<>());
        }

        /**
         * Get primary key(s) of the table
         *
         * @return names of columns
         */
        public List<String> getPrimaryKeys() {
            final List<String> primaryKeys = new ArrayList<>();
            columns.forEach((key, value) -> {
                if (value.isPrimaryKey) {
                    primaryKeys.add(key);
                }
            });
            return primaryKeys;
        }

        /**
         * Definitions usually starts by defining the primary key.
         *
         * @return columns sorted by {@code primaryKey} - primary keys are at the beginning.
         */
        @SuppressWarnings("all")
        public List<Column> getColumnsSortedByPrimaryKey() {
            return columns.values().stream().sorted((a, b) -> a.getIsPrimaryKey() ? -1 : 1).toList();
        }
    }

    /**
     * Represents database column inside a table.
     */
    @Data
    @AllArgsConstructor
    public static class Column {
        private String name;
        private String dataType;
        private Boolean isPrimaryKey;
        private ForeignKey foreignKey; // if not null, this column references another column in another table

        public Column(String name, String dataType, Boolean isPrimaryKey) {
            this(name, dataType, isPrimaryKey, null);
        }
    }

    /**
     * Foreign key / reference
     */
    public record ForeignKey(
            String referencedSchema,
            String referencedTable,
            String referencedColumn) {
        @JsonIgnore
        public String getReferencingString() {
            return " REFERENCES "
                    + (DEFAULT_SCHEMA.equals(referencedSchema) ? "" : referencedSchema + ".")
                    + referencedTable + "(" + referencedColumn + "),";
        }
    }
}