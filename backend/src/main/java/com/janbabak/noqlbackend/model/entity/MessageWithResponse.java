package com.janbabak.noqlbackend.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageWithResponse {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Chat chat;

    private String message;

    private String response;

    private Timestamp timestamp;
}
