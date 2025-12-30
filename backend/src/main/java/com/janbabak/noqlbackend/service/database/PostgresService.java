package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.PostgresDAO;
import com.janbabak.noqlbackend.dao.ResultSetWrapper;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Schema;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Table;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Column;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.ForeignKey;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.*;

/**
 * Retrieves Postgres database information.
 */
@Service
@Scope("prototype")
public class PostgresService extends SqlDatabaseService {

    public PostgresService(PostgresDAO postgresDAO) {
        super();
        databaseDAO = postgresDAO;
    }

    /**
     * Retrieves information about relations in the database represented by foreign keys.
     *
     * @param db database that already contains info about schemas, tables and columns
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    @Override
    protected void retrieveForeignKeys(SqlDatabaseStructure db)
            throws DatabaseConnectionException, DatabaseExecutionException {

        try (ResultSetWrapper result = databaseDAO.getForeignKeys()) {
            while (result.resultSet().next()) {
                final String constraintDefinition = result.resultSet().getString("constraint_definition");
                final String referencingSchemaAndTable = result.resultSet().getString(TABLE_NAME_COLUMN_NAME);
                final ForeignKeyData foreignKeyData = parseForeignKey(referencingSchemaAndTable, constraintDefinition);

                final Schema schema = db.schemas().get(foreignKeyData.referencingSchema);
                if (schema == null) {
                    continue;
                }
                Table table = schema.tables().get(foreignKeyData.referencingTable);
                if (table == null) {
                    // table not found, because table name is in double quotes
                    table = schema.tables().get(stripQuotes(foreignKeyData.referencingTable));
                    if (table == null) {
                        continue;
                    }
                }
                final Column column = table.columns().get(foreignKeyData.referencingColumn);
                if (column == null) {
                    continue;
                }
                column.setForeignKey(new ForeignKey(
                        foreignKeyData.referencedSchema,
                        foreignKeyData.referencedTable,
                        foreignKeyData.referencedColumn
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Parse foreign key - extract all the necessary information like referencing and referenced schema, table, column
     *
     * @param referencingSchemaAndTableName string that contains referencing schema and column
     * @param constraintDefinition          definition of the constraint from the database in the following format:
     *                                      {@code FOREIGN KEY (reviewer_of_data) REFERENCES cvut.student(id) }
     * @return foreign key data
     */
    private ForeignKeyData parseForeignKey(String referencingSchemaAndTableName, String constraintDefinition) {
        final Pair<String, String> referencingSchemaAndTableParsed = parseSchemaAndTable(referencingSchemaAndTableName);
        final String referencingSchema = referencingSchemaAndTableParsed.a;
        final String referencingTable = referencingSchemaAndTableParsed.b;

        // removes the "FOREIGN KEY (" prefix
        final String foreignKeySubstring = "FOREIGN KEY (";
        String constraint = constraintDefinition.substring(foreignKeySubstring.length());

        String referencingColumn = "";
        final String referencesSubstring = ") REFERENCES ";
        final int endOfReferencingColumnName = constraint.indexOf(referencesSubstring);
        if (endOfReferencingColumnName != -1) {
            referencingColumn = constraint.substring(0, endOfReferencingColumnName);
            constraint = constraint.substring(
                    endOfReferencingColumnName + referencesSubstring.length());
        }

        // if table or schema name contains "(" in its name, // doesn't work
        final int leftBraceIndex = constraint.indexOf("(");
        String referencedSchemaAndTable = "";
        if (leftBraceIndex != -1) {
            referencedSchemaAndTable = constraint.substring(0, leftBraceIndex);
            // + 1 to remove the "(" character
            constraint = constraint.substring(leftBraceIndex + 1);
        }
        final Pair<String, String> referencedSchemaAndTableParsed = parseSchemaAndTable(referencedSchemaAndTable);
        final String referencedSchema = referencedSchemaAndTableParsed.a;
        final String referencedTable = referencedSchemaAndTableParsed.b;

        // -1 to remove the ")" character
        final String referencedColumn = constraint.substring(0, constraintDefinition.length() - 1);

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
     *
     * @param data schema and table name together
     * @return [schemaName, tableName]
     */
    private Pair<String, String> parseSchemaAndTable(String data) {
        final int dotIndex = data.indexOf(".");

        // when dot is not found, table comes from the default (public) schema
        if (dotIndex == -1) {
            return new Pair<>(SqlDatabaseStructure.DEFAULT_SCHEMA, data);
        }

        // when dot is found, it usually separates the schema and table name (unless the schema name contains dot)
        final String schema = data.substring(0, dotIndex);
        final String table = data.substring(dotIndex + 1);

        return new Pair<>(schema, table);
    }

    /**
     * Strip double quotes from the string.
     *
     * @param s e.g. {@code s = "\"order\"";}
     * @return {@code "order"}
     */
    private String stripQuotes(String s) {
        if (s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private record ForeignKeyData(
            String referencingSchema,
            String referencingTable,
            String referencingColumn,
            String referencedSchema,
            String referencedTable,
            String referencedColumn
    ) {
    }
}
