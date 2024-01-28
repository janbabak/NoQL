package com.janbabak.noqlbackend.model.database;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * API response.
 */
@Data
@AllArgsConstructor
public class QueryResponse {
    private QueryResult result;
    private String generatedQuery;

    @Data
    public static class QueryResult {
        private final List<String> columnNames;
        private final List<List<String>> rows;

        public QueryResult(ResultSet resultSet) throws SQLException {
            ResultSetMetaData rsmd = resultSet.getMetaData();

            // columns
            columnNames = new ArrayList<>();
            for (int i = 1; i < rsmd.getColumnCount(); i++) {
                columnNames.add(rsmd.getColumnName(i));
            }

            // rows
            rows = new ArrayList<>();
            while (resultSet.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i < rsmd.getColumnCount(); i++) {
                    row.add(resultSet.getString(i));
                }
                rows.add(row);
            }
        }
    }
}
