package com.janbabak.noqlbackend.controller;


import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.database.Database;
import com.janbabak.noqlbackend.model.database.QueryResponse;
import com.janbabak.noqlbackend.model.database.UpdateDatabaseRequest;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.database.DatabaseEntityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping(value = "/database", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DatabaseController {

    private final DatabaseEntityService databaseService;
    private final QueryService queryService;

    /**
     * Get all databases.
     *
     * @return list of databases
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Database> getAll() {
        return databaseService.findAll();
    }

    /**
     * Get database by id.
     *
     * @param id identifier
     * @return database
     * @throws EntityNotFoundException database of specified id not found.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Database getById(@PathVariable UUID id) throws EntityNotFoundException {
        return databaseService.findById(id);
    }

    /**
     * Create new database.
     *
     * @param request database object (without id)
     * @return created object with its id.
     * @throws DatabaseConnectionException connection to the database failed (wrong credential, unavailable, etc.)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Database create(@RequestBody @Valid Database request) throws DatabaseConnectionException {
        return databaseService.create(request);
    }

    /**
     * Update database by id - set non-null fields.
     *
     * @param id      database identifier
     * @param request new data
     * @return updated database
     * @throws EntityNotFoundException     database of specified id not found.
     * @throws DatabaseConnectionException connection to the updated database failed.
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Database update(@PathVariable UUID id, @RequestBody @Valid UpdateDatabaseRequest request)
            throws EntityNotFoundException, DatabaseConnectionException {
        return databaseService.update(id, request);
    }

    /**
     * Delete database by id
     *
     * @param id database identifier
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable UUID id) {
        databaseService.deleteById(id);
    }

    /**
     * Query the user's database using natural language.
     *
     * @param id    database id
     * @param query natural language query
     * @return query result
     * @throws EntityNotFoundException     queried database not found.
     * @throws LLMException                LLM request failed.
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    @PostMapping("/{id}/query/natural-language")
    @ResponseStatus(HttpStatus.OK)
    public QueryResponse executeNaturalLanguageQuery(
            @PathVariable UUID id,
            @RequestBody String query,
            @RequestParam Integer page,
            @RequestParam Integer pageSize
    ) throws Exception {
        return queryService.executeSelectQuery(
                id, query, true, page, pageSize, false);
    }

    /**
     * Query the user's database using database query language.
     *
     * @param id    database id
     * @param query database query in corresponding database query language
     * @return query result
     * @throws EntityNotFoundException     queried database not found.
     * @throws LLMException                LLM request failed.
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     */
    @PostMapping("/{id}/query/query-language")
    @ResponseStatus(HttpStatus.OK)
    public QueryResponse executeQueryLanguageQuery(
            @PathVariable UUID id,
            @RequestBody String query,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Boolean overrideLimit
    ) throws Exception {
        return queryService.executeSelectQuery(id, query, false, page, pageSize, overrideLimit);
    }
}
