package com.janbabak.noqlbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chat {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Database database;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatQueryWithResponse> messages;

    private Timestamp modificationDate;

    private String name;

    public void addMessage(ChatQueryWithResponse newMessage) {
        for (ChatQueryWithResponse message: messages) {
            if (message.getId() == newMessage.getId()) {
                return;
            }
        }
        messages.add(newMessage);
    }

    // TODO: user reference
}
