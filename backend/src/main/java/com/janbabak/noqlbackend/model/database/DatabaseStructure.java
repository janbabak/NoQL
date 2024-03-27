package com.janbabak.noqlbackend.model.database;


/**
 * Represents a database object - contains information about schemas, tables, columns, primary keys, ...
 */
public interface DatabaseStructure {

    /**
     * Generates create script which can be help LLM understand the database schemas.
     *
     * @return insert script
     */
    String generateCreateScript();

    /**
     * Get data transfer object
     * @return DTO
     */
    DatabaseStructureDto toDto();
}
