package com.janbabak.noqlbackend.model.database;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Database object that contains also the structure.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DatabaseWithStructure extends Database {

    private DatabaseStructureDto databaseStructure;

    public DatabaseWithStructure(Database database, DatabaseStructureDto databaseStructure) {
        super(database);
        this.databaseStructure = databaseStructure;
    }
}
