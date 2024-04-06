package com.janbabak.noqlbackend.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Data
public class Chat {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Database database;

    @OneToMany
    private List<MessageWithResponse> messages;

    private Timestamp modificationDate;

    public void addMessage(MessageWithResponse newMessage) {
        for (MessageWithResponse message: messages) {
            if (message.getId() == newMessage.getId()) {
                return;
            }
        }
        messages.add(newMessage);
    }

    // TODO: user reference
}
