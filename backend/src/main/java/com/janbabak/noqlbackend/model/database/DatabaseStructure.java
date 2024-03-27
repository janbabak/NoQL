package com.janbabak.noqlbackend.model.database;

import java.util.List;
import java.util.Map;

/**
 * Represents a database object - contains information about schemas, tables, columns, primary keys, ...
 */
public interface DatabaseStructure {

    /**
     * Generates create script which can be help LLM understand the database schemas.
     *
     * @return insert script
     */
    public String generateCreateScript();
}
