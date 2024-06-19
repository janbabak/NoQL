package com.janbabak.noqlbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.validation.FirstValidationGroup;
import com.janbabak.noqlbackend.validation.SecondValidationGroup;
import com.janbabak.noqlbackend.validation.ValidHostName;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

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
    @NotBlank(groups = FirstValidationGroup.class)
    @Length(min = 1, max = 32, groups = SecondValidationGroup.class)
    private String name;

    @NotBlank(groups = FirstValidationGroup.class)
    @ValidHostName(groups = SecondValidationGroup.class)
    private String host;

    @NotNull(groups = FirstValidationGroup.class)
    @Min(value = 1, groups = SecondValidationGroup.class)
    private Integer port;

    /**
     * Name of the database to connect to (used in the connection URL).
     */
    @NotBlank(groups = FirstValidationGroup.class)
    @Length(min = 1, max = 253, groups = SecondValidationGroup.class)
    private String database;

    @NotBlank(groups = FirstValidationGroup.class)
    @Length(min = 1, max = 128, groups = SecondValidationGroup.class)
    private String userName;

    @NotBlank(groups = FirstValidationGroup.class)
    @Length(min = 1, max = 128, groups = SecondValidationGroup.class) // TODO: is 128 enough?
    private String password;

    @NotNull(groups = FirstValidationGroup.class)
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
