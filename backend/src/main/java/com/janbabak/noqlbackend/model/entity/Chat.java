package com.janbabak.noqlbackend.model.entity;

import com.janbabak.noqlbackend.validation.FirstValidationGroup;
import com.janbabak.noqlbackend.validation.SecondValidationGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

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
    private List<ChatQueryWithResponse> messages;

    private Timestamp modificationDate;

    @NotBlank(groups = FirstValidationGroup.class)
    @Length(min = 1, max = 32, groups = SecondValidationGroup.class)
    private String name;

    public void addMessage(ChatQueryWithResponse newMessage) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        for (ChatQueryWithResponse message: messages) {
            if (message.getId() != null && message.getId() == newMessage.getId()) {
                return;
            }
        }
        messages.add(newMessage);
        newMessage.setChat(this);
    }

    /**
     * Add collection of new messages to the chat.
     * @param newMessages new messages
     */
    public void addMessages(Collection<ChatQueryWithResponse> newMessages) {
        for (ChatQueryWithResponse message: newMessages) {
            addMessage(message);
        }
    }
}
