package com.janbabak.noqlbackend.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
public class Database {
    public final static String DEFAULT_SCHEMA = "public";
    private Map<String, Schema> schemas;


    public Database() {
        schemas = new HashMap<>();
        schemas.put(DEFAULT_SCHEMA, new Schema(DEFAULT_SCHEMA));
    }

    /**
     * Generates create script which can be help LLM understand the database schemas
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

                // columns
                for (Map.Entry<String, Column> columnEntry : tableEntry.getValue().getColumns().entrySet()) {
                    script
                            .append("\n\t")
                            .append(columnEntry.getValue().getName())
                            .append(" ")
                            .append(columnEntry.getValue().getDataType().toUpperCase())
                            .append(columnEntry.getValue().getPrimaryKey() ? " PRIMARY KEY," : "")
                            .append(columnEntry.getValue().getForeignKey() != null
                                    ? columnEntry.getValue().getForeignKey().getReferencingString()
                                    : ""
                            );
                }

                script.append("\n);\n");
            }
        }
        return script.toString();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Schema {
        private String name;
        private Map<String, Table> tables;

        public Schema(String name) {
            this.name = name;
            this.tables = new HashMap<>();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Table {
        private String name;
        private Map<String, Column> columns;

        public Table(String name) {
            this.name = name;
            this.columns = new HashMap<>();
        }
    }

    @Data
    @AllArgsConstructor
    public static class Column {
        private String name;
        private String dataType;
        private Boolean primaryKey;
        private ForeignKey foreignKey; // if not null, this column references another column in another table

        public Column(String name, String dataType, Boolean primaryKey) {
            this.name = name;
            this.dataType = dataType;
            this.primaryKey = primaryKey;
            this.foreignKey = null;
        }
    }

    @Data
    @AllArgsConstructor
    public static class ForeignKey {
        private String referencedSchema;
        private String referencedTable;
        private String referencedColumn;

        public String getReferencingString() {
            return " REFERENCES "
                    + (referencedSchema.equals(DEFAULT_SCHEMA) ? "" : referencedSchema + ".")
                    + referencedTable + "(" + referencedColumn  + "),";
        }
    }
}
