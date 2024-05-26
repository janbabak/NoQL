package com.janbabak.noqlbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
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

    @JsonIgnore // to avoid infinite recursion or creation of DTO object
    @OneToMany(
            mappedBy = "database",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Chat> chats;

    @NotNull
    private Boolean isSQL; // TODO: remove column, can be inferred from engine
}
