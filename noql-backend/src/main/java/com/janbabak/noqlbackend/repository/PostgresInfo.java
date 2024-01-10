package com.janbabak.noqlbackend.repository;

import com.janbabak.noqlbackend.db.Schema;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class PostgresInfo implements DatabaseInfo {

    private static final String TABLE_SCHEMA = "table_schema";
    private static final String TABLE_NAME = "table_name";
    private static final String COLUMN_NAME = "column_name";
    private static final String DATA_TYPE = "data_type";

    public void getSchema() throws SQLException {
        Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/postgres", "user", "password");
        Statement statement = connection.createStatement();
        String select = """
                SELECT table_schema, table_name, column_name, data_type
                FROM information_schema.columns
                WHERE table_schema NOT LIKE 'pg_%'
                  AND table_schema != 'information_schema'
                  AND table_name IN (SELECT table_name
                                     FROM information_schema.tables
                                     WHERE table_type = 'BASE TABLE'
                                       AND table_catalog = current_database())
                ORDER BY table_schema, table_name, ordinal_position;
                """;

        ResultSet resultSet = statement.executeQuery(select);

        Map<String, Schema> schemas = new HashMap<>();

        while (resultSet.next()) {
            String tableSchema = resultSet.getString(TABLE_SCHEMA);
            String tableName = resultSet.getString(TABLE_NAME);
            String columnName = resultSet.getString(COLUMN_NAME);
            String dataType = resultSet.getString(DATA_TYPE);

            Schema schema = schemas.get(tableSchema);
            if (schema == null) {
                schema = new Schema(tableSchema);
                schemas.put(tableSchema, schema);
            }

            Schema.Table table = schema.getTables().get(tableName);
            if (table == null) {
                table = new Schema.Table(tableName);
                schema.getTables().put(tableName, table);
            }

            table.getColumns().add(new Schema.Column(columnName, dataType));

//            System.out.println(resultSet.getString("table_schema") + " " + resultSet.getString("table_name") + " " +
//                    resultSet.getString("column_name") + " " + resultSet.getString("data_type"));
        }

        for (Map.Entry<String, Schema> entry :
                schemas.entrySet()) {
            System.out.println(entry.getValue());
        }

    }
}
