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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@Scope("prototype")
public class MySqlService extends SqlDatabaseService {

    public MySqlService(MySqlDAO mySqlDAO) {
        databaseDAO = mySqlDAO;
    }

    protected void retrieveForeignKeys(SqlDatabaseStructure dbStructure)
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

                Schema schema = dbStructure.schemas().get(referencingSchema);
                if (schema == null) {
                    continue;
                }
                Table table = schema.tables().get(referencingTable);
                if (table == null) {
                    continue;
                }
                Column column = table.columns().get(referencingColumn);
                if (column == null) {
                    continue;
                }
                column.setForeignKey(new ForeignKey(referencedSchema, referencedTable, referencedColumn));
            }
        } catch (SQLException e) {
            throw new DatabaseExecutionException(e.getMessage());
        }
    }
}
