package com.janbabak.noqlbackend.model.database;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Database object that contains also the structure.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DatabaseWithStructure extends Database {

    private DatabaseStructure databaseStructure;

    public DatabaseWithStructure(Database database, DatabaseStructure databaseStructure) {
        super(database);
        this.databaseStructure = databaseStructure;
    }
}
