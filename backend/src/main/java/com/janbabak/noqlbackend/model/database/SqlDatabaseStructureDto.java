package com.janbabak.noqlbackend.model.database;

import java.util.List;

/**
 * SQL database structure data transfer object - contains arrays not maps as the {@link  SqlDatabaseStructure}
 */
public record SqlDatabaseStructureDto(List<SchemaDto> schemas) implements DatabaseStructureDto {

    public SqlDatabaseStructureDto(SqlDatabaseStructure sqlDatabaseStructure) {
        this(sqlDatabaseStructure.schemas().values().stream().map(SchemaDto::new).toList());
    }

    public record SchemaDto(
            String name,
            List<TableDto> tables) {
        public SchemaDto(SqlDatabaseStructure.Schema schema) {
            this(schema.name(), schema.tables().values().stream().map(TableDto::new).toList());
        }
    }

    public record TableDto(
            String name,
            List<SqlDatabaseStructure.Column> columns) {
        public TableDto(SqlDatabaseStructure.Table table) {
            this(table.name(), table.columns().values().stream().toList());
        }
    }
}
