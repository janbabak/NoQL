package com.janbabak.noqlbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
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

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatQueryWithResponse> messages = new ArrayList<>();

    private Timestamp modificationDate;

    private String name;

    public Chat addMessage(ChatQueryWithResponse newMessage) {
        for (ChatQueryWithResponse message: messages) {
            if (message.getId() == newMessage.getId()) {
                return this;
            }
        }
        messages.add(newMessage);
        return this;
    }

    // TODO: user reference
}
