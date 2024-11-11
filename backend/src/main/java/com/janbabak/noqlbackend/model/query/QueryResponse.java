package com.janbabak.noqlbackend.model.query;

import com.janbabak.noqlbackend.model.chat.ChatQueryWithResponseDto;
import lombok.Builder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Response with retrieved data.
 */
@Builder
public record QueryResponse(
        RetrievedData data,
        Long totalCount, // total count of rows (response is paginated, so it does not contain all of them)
        ChatQueryWithResponseDto chatQueryWithResponse, // last chat query with LLM response
        String errorMessage /* error message when the query execution failed due to syntax error */) {

    public static QueryResponse successfulResponse(
            RetrievedData resultData, ChatQueryWithResponseDto message, Long totalCount) {
        return new QueryResponse(resultData, totalCount, message, null);
    }

    public static QueryResponse failedResponse(ChatQueryWithResponseDto message, String errorMessage) {
        return new QueryResponse(null, null, message, errorMessage);
    }

    public record RetrievedData(
            List<String> columnNames,
            List<List<String>> rows) {

        public RetrievedData(ResultSet resultSet) throws SQLException {
            this(new ArrayList<>(), new ArrayList<>());

            ResultSetMetaData rsmd = resultSet.getMetaData();

            // columns
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                columnNames.add(rsmd.getColumnName(i));
            }

            // rows
            while (resultSet.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    row.add(resultSet.getString(i).trim());
                }
                rows.add(row);
            }
        }
    }
}
