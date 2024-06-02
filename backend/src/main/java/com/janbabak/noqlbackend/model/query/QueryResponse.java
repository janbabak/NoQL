package com.janbabak.noqlbackend.model.query;

import com.janbabak.noqlbackend.model.chat.ChatQueryWithResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@AllArgsConstructor
public class QueryResponse {

    private RetrievedData data;
    private Long totalCount; // total count of rows (response is paginated, so it does not contain all of them)
    private ChatQueryWithResponseDto chatQueryWithResponse; // last chat query with LLM response
    private String errorMessage; // error message when the query execution failed due to syntax error

    public static QueryResponse successfulResponse(
            RetrievedData resultData, ChatQueryWithResponseDto message, Long totalCount) {
        return new QueryResponse(resultData, totalCount, message,null);
    }

    public static QueryResponse failedResponse(ChatQueryWithResponseDto message, String errorMessage) {
        return new QueryResponse(null, null, message, errorMessage);
    }

    @Data
    @AllArgsConstructor
    public static class RetrievedData {
        private final List<String> columnNames;
        private final List<List<String>> rows;

        public RetrievedData(ResultSet resultSet) throws SQLException {
            ResultSetMetaData rsmd = resultSet.getMetaData();

            // columns
            columnNames = new ArrayList<>();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                columnNames.add(rsmd.getColumnName(i));
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
