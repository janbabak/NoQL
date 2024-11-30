package com.janbabak.noqlbackend.model.query;

import lombok.Builder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Builder
public record ChatResponseData( //TODO: rename to RetrievedData
        List<String> columnNames,
        List<List<String>> rows,
        Integer page,
        Integer pageSize,
        Long totalCount
) {

    /**
     * Create ChatResponseData from ResultSet
     *
     * @param resultSet  result of the query
     * @param page       page number (starting from 0)
     * @param pageSize   number of rows on one page
     * @param totalCount total count of rows (response is paginated, so it does not contain all of them)
     * @throws SQLException when the result set is not valid
     */
    public ChatResponseData(ResultSet resultSet, Integer page, Integer pageSize, Long totalCount)
            throws SQLException {

        this(new ArrayList<>(), new ArrayList<>(), page, pageSize, totalCount);

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
