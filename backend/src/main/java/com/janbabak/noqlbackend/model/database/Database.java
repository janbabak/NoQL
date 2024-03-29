package com.janbabak.noqlbackend.model.database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Represents customer's database.
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Database {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Size(min = 1, max = 32)
    private String name;

    @NotBlank
    private String host;

    @Min(1)
    private Integer port;

    @NotBlank
    private String database;

    @NotBlank
    private String userName;

    @NotBlank
    private String password;

    @NotNull
    private DatabaseEngine engine;

    @NotNull
    private Boolean isSQL;

    public Database(Database from) {
        this(
                from.id,
                from.name,
                from.host,
                from.port,
                from.database,
                from.userName,
                from.password,
                from.engine,
                from.isSQL
        );
    }
}
