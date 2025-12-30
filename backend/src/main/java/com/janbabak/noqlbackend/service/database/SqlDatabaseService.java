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
    @Override
    public SqlDatabaseStructure retrieveSchema() throws DatabaseConnectionException, DatabaseExecutionException {

        final SqlDatabaseStructure dbStructure = new SqlDatabaseStructure();
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
            final ResultSet resultSet = result.resultSet();
            while (resultSet.next()) {
                final String tableSchema = resultSet.getString(TABLE_SCHEMA_COLUMN_NAME);
                final String tableName = resultSet.getString(TABLE_NAME_COLUMN_NAME);
                final String columnName = resultSet.getString(COLUMN_NAME_COLUMN_NAME);
                final String dataType = resultSet.getString(DATA_TYPE_COLUMN_NAME);
                final Boolean primaryKey = resultSet.getBoolean(PRIMARY_KEY_COLUMN_NAME);

                final SqlDatabaseStructure.Schema schema =
                        dbStructure.schemas().computeIfAbsent(tableSchema, SqlDatabaseStructure.Schema::new);

                final SqlDatabaseStructure.Table table =
                        schema.tables().computeIfAbsent(tableName, SqlDatabaseStructure.Table::new);

                // columnName key definitely doesn't exist - no need of checking it out
                table.columns().put(columnName, new SqlDatabaseStructure.Column(columnName, dataType, primaryKey));
            }
        } catch (SQLException e) {
            throw new DatabaseExecutionException(e.getMessage(), e);
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
