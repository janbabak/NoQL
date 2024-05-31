package com.janbabak.noqlbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
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

    /**
     * User-defined name of the database.
     */
    @NotBlank
    @Size(min = 1, max = 32)
    private String name;

    @NotBlank
    private String host;

    @Min(1)
    private Integer port;

    /**
     * Name of the database to connect to (used in the connection URL).
     */
    @NotBlank
    private String database;

    @NotBlank
    private String userName;

    @NotBlank
    private String password;

    @NotNull
    private DatabaseEngine engine;

    @JsonIgnore // to avoid infinite recursion or the creation of a DTO object
    @OneToMany(
            mappedBy = "database",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Chat> chats;

    /**
     * @return true if database engine is SQL
     */
    @JsonIgnore
    @SuppressWarnings("all") // 'default' branch is unnecessary
    public Boolean isSQL() {
        return switch (engine) {
            case POSTGRES, MYSQL -> true;
            default -> false;
        };
    }

    public void addChat(Chat chat) {
        if (chats == null) {
            chats = new ArrayList<>();
        }
        for (Chat c: chats) {
            if (chat.getId() != null && c.getId().equals(chat.getId())) {
                return;
            }
        }
        chats.add(chat);
        chat.setDatabase(this);
    }

    /**
     * Add collection of chats
     * @param newChats new chats
     */
    public void addChats(Collection<Chat> newChats) {
        for (Chat chat: newChats) {
            addChat(chat);
        }
    }
}
