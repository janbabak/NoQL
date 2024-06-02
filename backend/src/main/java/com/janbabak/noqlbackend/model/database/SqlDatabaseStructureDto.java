package com.janbabak.noqlbackend.model.database;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * SQL database structure data transfer object - contains arrays not maps as the {@link  SqlDatabaseStructure}
 */
@Data
@AllArgsConstructor
public class SqlDatabaseStructureDto implements DatabaseStructureDto {

    private final List<SchemaDto> schemas;

    public SqlDatabaseStructureDto(SqlDatabaseStructure sqlDatabaseStructure) {
        schemas = sqlDatabaseStructure.getSchemas().values().stream().map(SchemaDto::new).toList();
    }

    @Data
    @AllArgsConstructor
    public static class SchemaDto {
        private final String name;
        private final List<TableDto> tables;

        public SchemaDto(SqlDatabaseStructure.Schema schema) {
            this.name = schema.getName();
            this.tables = schema.getTables().values().stream().map(TableDto::new).toList();
        }
    }

    @Data
    @AllArgsConstructor
    public static class TableDto {
        private final String name;
        private final List<SqlDatabaseStructure.Column> columns;

        public TableDto(SqlDatabaseStructure.Table table) {
            this.name = table.getName();
            this.columns = table.getColumns().values().stream().toList();
        }
    }
}
