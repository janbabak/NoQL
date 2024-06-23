package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.entity.Database;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MySqlDAO extends DatabaseDAO {

    public MySqlDAO(Database database) {
        super(database);
    }

    /**
     * Retrieve database schemas, tables columns and primary keys.<br />
     * Returned columns:<br />
     * <ul>
     *     <li>table_schema e.g. cvut</li>
     *     <li>table_name e.g. student</li>
     *     <li>column_name e.g. student_specialization_id</li>
     *     <li>data_type e.g. int</li>
     *     <li>primary_key e.g. true</li>
     * </ul>
     *
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    @Override
    @SuppressWarnings("all") // IDE can't see the columns
    public ResultSetWrapper getSchemasTablesColumns() throws DatabaseConnectionException, DatabaseExecutionException {
        // language=SQL
        String select = """
                SELECT
                    t.TABLE_SCHEMA AS table_schema,
                    t.TABLE_NAME AS table_name,
                    c.COLUMN_NAME AS column_name,
                    c.DATA_TYPE AS data_type,
                    IF(kcu.COLUMN_NAME IS NOT NULL, 'true', 'false') AS primary_key
                FROM
                    information_schema.TABLES t
                    JOIN information_schema.COLUMNS c
                        ON t.TABLE_SCHEMA = c.TABLE_SCHEMA
                        AND t.TABLE_NAME = c.TABLE_NAME
                    LEFT JOIN information_schema.KEY_COLUMN_USAGE kcu
                        ON t.TABLE_SCHEMA = kcu.TABLE_SCHEMA
                        AND t.TABLE_NAME = kcu.TABLE_NAME
                        AND c.COLUMN_NAME = kcu.COLUMN_NAME
                        AND kcu.CONSTRAINT_NAME = 'PRIMARY'
                WHERE
                    t.TABLE_TYPE = 'BASE TABLE'
                    AND t.TABLE_SCHEMA NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys')
                ORDER BY
                    t.TABLE_SCHEMA,
                    t.TABLE_NAME,
                    c.ORDINAL_POSITION;
                """;

        return query(select);
    }

    /**
     * Retrieve foreign keys.<br />
     * Returned columns:<br />
     * <ul>
     *     <li>referencing_schema e.g. cvut</li>
     *     <li>referencing_table e.g. student</li>
     *     <li>referencing_column e.g. student_specialization_id</li>
     *     <li>referenced_schema e.g. cvut</li>
     *     <li>referenced_table e.g. specialization</li>
     *     <li>referenced_column e.g. id</li>
     * </ul>
     *
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    @Override
    @SuppressWarnings("all") // IDE can't see the columns
    public ResultSetWrapper getForeignKeys() throws DatabaseConnectionException, DatabaseExecutionException {
        // language=SQL
        String select = """
                SELECT
                    kcu.TABLE_SCHEMA AS referencing_schema,
                    kcu.TABLE_NAME AS referencing_table,
                    kcu.COLUMN_NAME AS referencing_column,
                    kcu.REFERENCED_TABLE_SCHEMA AS referenced_schema,
                    kcu.REFERENCED_TABLE_NAME AS referenced_table,
                    kcu.REFERENCED_COLUMN_NAME AS referenced_column
                FROM
                    information_schema.KEY_COLUMN_USAGE kcu
                    JOIN information_schema.REFERENTIAL_CONSTRAINTS rc
                        ON kcu.CONSTRAINT_NAME = rc.CONSTRAINT_NAME
                        AND kcu.TABLE_SCHEMA = rc.CONSTRAINT_SCHEMA
                WHERE
                    kcu.REFERENCED_TABLE_SCHEMA IS NOT NULL
                    AND kcu.TABLE_SCHEMA NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys')
                ORDER BY
                    kcu.TABLE_SCHEMA,
                    kcu.TABLE_NAME,
                    kcu.
                        COLUMN_NAME;
                        
                """;

        return query(select);
    }

    /**
     * Test connection to database.
     *
     * @throws DatabaseConnectionException cannot establish connection with the database
     */
    @Override
    public void testConnection() throws DatabaseConnectionException {
        connect(true);

        if (connection == null) {
            throw new DatabaseConnectionException();
        } // TODO: move to ancestor
    }

    /**
     * Create connection URL for specific database engine.
     *
     * @return connection URL
     */
    @Override
    protected String createConnectionUrl() {
        return "jdbc:mysql://" + databaseMetadata.getHost() + ":" + databaseMetadata.getPort() +
                "/" + databaseMetadata.getDatabase(); // TODO: formatted string
    }
}
