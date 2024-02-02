package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.DatabaseDAO;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.model.database.DatabaseStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;

/**
 * This service handles operations with database using the {@link DatabaseDAO } with standard Connection as a DAO.
 * Different database engines are handled by different implementations.
 */
@Service
public abstract class BaseDatabaseService {
    protected DatabaseDAO databaseDAO; // initialized by the descendant class

    @Autowired
    @SuppressWarnings("all")
    protected DatabaseRepository databaseRepository;

    /**
     * Execute SQL or some other query (depends on the underling database engine)
     *
     * @param query query string
     * @return result set
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException query execution failed (syntax error)
     */
    public ResultSet executeQuery(String query) throws DatabaseConnectionException, DatabaseExecutionException {
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
