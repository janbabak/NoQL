package com.janbabak.noqlbackend.controller;


import com.janbabak.noqlbackend.model.database.Database;
import com.janbabak.noqlbackend.service.database.BaseDatabaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/database", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DatabaseController {

    private final BaseDatabaseService databaseService;

    /**
     * Get all databases.
     * @return list of databases
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Database> getAll() {
        return databaseService.findAll();
    }

    /**
     * Get database by id.
     * @param id identifier
     * @return database
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Database getById(@PathVariable UUID id) {
        return databaseService.findById(id);
    }

    /**
     * Create new database.
     * @param request database object (without id)
     * @return created object with its id.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Database create(@RequestBody Database request) {
        return databaseService.create(request);
    }

    /**
     * Update not null fields by database id.
     * @param id database identifier
     * @param request new data
     * @return updated database
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Database update(@PathVariable UUID id, @RequestBody Database request) throws Exception {
        return databaseService.update(id, request);
    }

    /**
     * Delete database by id
     * @param id database identifier
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable UUID id) {
        databaseService.deleteById(id);
    }

    // TODO: delete
    @GetMapping("insertSample")
    public String insertSampleData() {
        databaseService.insertSampleData();
        return "inserted";
    }
}
