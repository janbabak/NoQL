package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.ResultSetWrapper;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.database.DatabaseStructure;
import com.janbabak.noqlbackend.model.entity.Database;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service handles operations with database using the {@link DatabaseDAO } with standard Connection as a DAO.
 * Different database engines are handled by different implementations.
 */
@Service
@RequiredArgsConstructor
public abstract class BaseDatabaseService {
    protected DatabaseDAO databaseDAO; // initialized by the descendant class

    @SuppressWarnings("all")
    @Autowired
    protected DatabaseRepository databaseRepository;

    public void setDatabaseDaoMetadata(Database database) {
        databaseDAO.setDatabaseMetadata(database);
    }

    /**
     * Execute SQL or some other query (depends on the underling database engine)
     *
     * @param query query string
     * @return result set
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException query execution failed (syntax error)
     */
    public ResultSetWrapper executeQuery(String query) throws DatabaseConnectionException, DatabaseExecutionException {
        return databaseDAO.query(query);
    }

    /**
     * Retrieves database schema (structure of tables, columns, etc...)
     *
     * @return database structure
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException query execution failed (syntax error)
     */
    abstract public DatabaseStructure retrieveSchema() throws DatabaseConnectionException, DatabaseExecutionException;
}
