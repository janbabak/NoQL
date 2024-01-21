package com.janbabak.noqlbackend.model.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Database {
    private String id;
    private String host;
    private String port;
    private String database;
    private String user;
    private String password;
    private DatabaseEngine engine;
    private Boolean isSQL;
}
