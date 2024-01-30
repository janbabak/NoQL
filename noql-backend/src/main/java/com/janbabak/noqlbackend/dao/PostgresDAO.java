package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.model.database.Database;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresDAO extends DatabaseDAO {

    public PostgresDAO(Database database) {
        super(database);
    }

    @SuppressWarnings("unused")
    public PostgresDAO() {
        super();
    }

    @Override
    protected String createConnectionUrl() {
        return "jdbc:postgresql://" + databaseMetadata.getHost() + ":" + databaseMetadata.getPort() +
                "/" + databaseMetadata.getDatabase();
    }

    @Override
    public ResultSet getSchemasTablesColumns() {
        // language=SQL
        String select = """
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

        return query(select);
    }

    @Override
    public ResultSet getForeignKeys() {
        // language=SQL
        String select = """
                SELECT conrelid::regclass AS table_name,
                       conname AS foreign_key,
                       pg_get_constraintdef(oid) AS constraint_definition
                FROM   pg_constraint
                WHERE  contype = 'f'
                ORDER  BY conrelid::regclass::text, contype DESC;
                """;

        return query(select);
    }

    @Override
    public void testConnection() throws DatabaseConnectionException {
        try {
            connect();
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e.getMessage());
        }
        if (connection == null) {
            throw new DatabaseConnectionException();
        }
    }
}
