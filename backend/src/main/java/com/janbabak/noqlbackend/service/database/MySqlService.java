package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.MySqlDAO;
import com.janbabak.noqlbackend.dao.ResultSetWrapper;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Schema;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Table;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.Column;
import com.janbabak.noqlbackend.model.database.SqlDatabaseStructure.ForeignKey;
import com.janbabak.noqlbackend.model.entity.Database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlService extends BaseDatabaseService {

    private static final String TABLE_SCHEMA_COLUMN_NAME = "table_schema";
    private static final String TABLE_NAME_COLUMN_NAME = "table_name";
    private static final String COLUMN_NAME_COLUMN_NAME = "column_name";
    private static final String DATA_TYPE_COLUMN_NAME = "data_type";
    private static final String PRIMARY_KEY_COLUMN_NAME = "primary_key";

    public MySqlService(Database database) {
        databaseDAO = new MySqlDAO(database);
    }

    /**
     * Retrieves database schema (structure of tables, columns, etc...)
     *
     * @return database structure
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    @Override
    public SqlDatabaseStructure retrieveSchema() throws DatabaseConnectionException, DatabaseExecutionException {

        SqlDatabaseStructure dbStructure = new SqlDatabaseStructure();

        retrieveSchemasTablesColumns(dbStructure);
        retrieveForeignKeys(dbStructure);

        return dbStructure;
    }

    // TODO: move to sql database service ancestor
    /**
     * Retrieves database information about schemas, tables and columns, primary keys, (omits relations)
     *
     * @param dbStructure empty database structure
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    private void retrieveSchemasTablesColumns(SqlDatabaseStructure dbStructure)
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

    private void retrieveForeignKeys(SqlDatabaseStructure dbStructure)
            throws DatabaseConnectionException, DatabaseExecutionException {

        try (ResultSetWrapper result = databaseDAO.getForeignKeys()) {
            ResultSet resultSet = result.resultSet();
            while (resultSet.next()) {
                String referencingSchema = resultSet.getString("referencing_schema");
                String referencingTable = resultSet.getString("referencing_table");
                String referencingColumn = resultSet.getString("referencing_column");
                String referencedSchema = resultSet.getString("referenced_schema");
                String referencedTable = resultSet.getString("referenced_table");
                String referencedColumn = resultSet.getString("referenced_column");

                Schema schema = dbStructure.getSchemas().get(referencingSchema);
                if (schema == null) {
                    continue;
                }
                Table table = schema.getTables().get(referencingTable);
                if (table == null) {
                    continue;
                }
                Column column = table.getColumns().get(referencingColumn);
                if (column != null) {
                    continue;
                }
                column.setForeignKey(new ForeignKey(referencedSchema, referencedTable, referencedColumn));
            }
        } catch (SQLException e) {
            throw new DatabaseExecutionException(e.getMessage());
        }
    }
}
