package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.ResultSetWrapper;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SqlDatabaseService extends BaseDatabaseService {
    protected static final String TABLE_SCHEMA_COLUMN_NAME = "table_schema";
    protected static final String TABLE_NAME_COLUMN_NAME = "table_name";
    protected static final String COLUMN_NAME_COLUMN_NAME = "column_name";
    protected static final String DATA_TYPE_COLUMN_NAME = "data_type";
    protected static final String PRIMARY_KEY_COLUMN_NAME = "primary_key";

    /**
     * Retrieves information about database schema - schemas, tables, columns, primary and foreign keys, ...
     *
     * @return database information
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    public SqlDatabaseStructure retrieveSchema() throws DatabaseConnectionException, DatabaseExecutionException {

        SqlDatabaseStructure dbStructure = new SqlDatabaseStructure();
        retrieveSchemasTablesColumns(dbStructure);
        retrieveForeignKeys(dbStructure);

        return dbStructure;
    }

    /**
     * Retrieves database information about schemas, tables and columns, primary keys, (omits relations)
     *
     * @param dbStructure empty database
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    protected void retrieveSchemasTablesColumns(SqlDatabaseStructure dbStructure)
            throws DatabaseConnectionException, DatabaseExecutionException {
        try (ResultSetWrapper result = databaseDAO.getSchemasTablesColumns()) {
            ResultSet resultSet = result.resultSet();
            while (resultSet.next()) {
                String tableSchema = resultSet.getString(TABLE_SCHEMA_COLUMN_NAME);
                String tableName = resultSet.getString(TABLE_NAME_COLUMN_NAME);
                String columnName = resultSet.getString(COLUMN_NAME_COLUMN_NAME);
                String dataType = resultSet.getString(DATA_TYPE_COLUMN_NAME);
                Boolean primaryKey = resultSet.getBoolean(PRIMARY_KEY_COLUMN_NAME);

                SqlDatabaseStructure.Schema schema =
                        dbStructure.getSchemas().computeIfAbsent(tableSchema, SqlDatabaseStructure.Schema::new);

                SqlDatabaseStructure.Table table =
                        schema.getTables().computeIfAbsent(tableName, SqlDatabaseStructure.Table::new);

                // columnName key definitely doesn't exist - no need of checking it out
                table.getColumns().put(columnName, new SqlDatabaseStructure.Column(columnName, dataType, primaryKey));
            }
        } catch (SQLException e) {
            throw new DatabaseExecutionException(e.getMessage());
        }
    }

    /**
     * Retrieves information about relations in the database represented by foreign keys.
     *
     * @param db database that already contains info about schemas, tables and columns
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    protected abstract void retrieveForeignKeys(SqlDatabaseStructure db)
            throws DatabaseConnectionException, DatabaseExecutionException;
}
