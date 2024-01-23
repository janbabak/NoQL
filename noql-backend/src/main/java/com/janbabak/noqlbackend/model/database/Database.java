package com.janbabak.noqlbackend.model.database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Database {

    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String host;
    private String port;
    private String database;
    private String userName;
    private String password;
    private DatabaseEngine engine;
    private Boolean isSQL;
}
