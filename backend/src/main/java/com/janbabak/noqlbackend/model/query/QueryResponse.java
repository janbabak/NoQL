package com.janbabak.noqlbackend.model.query;

import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Response with retrieved data.
 */
@Data
@AllArgsConstructor
public class QueryResponse {
    private QueryResult result; // retrieved data
    private Long totalCount; // total count of rows (response is paginated, so it does not contain all of them)
    private ColumnTypes columnTypes; // which columns are categorical, numeric, ...
    private ChatQueryWithResponseDto chatQueryWithResponse; // last chat query with LLM response
    private String errorMessage; // error message when the query execution failed due to syntax error

    public static QueryResponse successfulResponse(
            QueryResult result, ChatQueryWithResponseDto message, Long totalCount, ColumnTypes columnTypes) {
        return new QueryResponse(result, totalCount, columnTypes, message,null);
    }

    public static QueryResponse failedResponse(ChatQueryWithResponseDto message, String errorMessage) {
        return new QueryResponse(null, null, null, message, errorMessage);
    }

    @Data
    public static class QueryResult {
        private final List<String> columnNames;
        private final List<List<String>> rows;

        public QueryResult(ResultSet resultSet) throws SQLException {
            ResultSetMetaData rsmd = resultSet.getMetaData();

            // columns
            columnNames = new ArrayList<>();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                columnNames.add(rsmd.getColumnName(i));
                System.out.println("TYPE: " + rsmd.getColumnType(i));
            }

            // rows
            rows = new ArrayList<>();
            while (resultSet.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    row.add(resultSet.getString(i));
                }
                rows.add(row);
            }
        }
    }

    @Data
    public static class ColumnTypes {
        private final List<String> categorical = new ArrayList<>();
        private final List<String> numeric = new ArrayList<>();
        private final List<String> timestamp = new ArrayList<>();

        public void addCategorical(String columnName) {
            categorical.add(columnName);
        }

        public void addNumeric(String columnName) {
            numeric.add(columnName);
        }

        public void addTimestamp(String columnName) {
            timestamp.add(columnName);
        }
    }
}
