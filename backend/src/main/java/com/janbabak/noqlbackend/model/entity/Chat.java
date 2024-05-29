package com.janbabak.noqlbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
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

    public void addMessage(ChatQueryWithResponse newMessage) {
        for (ChatQueryWithResponse message: messages) {
            if (message.getId() != null && message.getId() == newMessage.getId()) {
                return;
            }
        }
        messages.add(newMessage);
        newMessage.setChat(this);
    }

    /**
     * Add colledtion of new messages to the chat.
     * @param newMessages new messages
     */
    public void addMessages(Collection<ChatQueryWithResponse> newMessages) {
        for (ChatQueryWithResponse message: newMessages) {
            addMessage(message);
        }
    }

    // TODO: user reference
}
