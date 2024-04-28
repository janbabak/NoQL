package com.janbabak.noqlbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatQueryWithResponse {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Chat chat;

    private String message;

    /** json in form of string { {@code  databaseQuery: string, generatePlot: boolean, pythonCode: string }} */
    @Column(length = 2048)
    private String response;

    private Timestamp timestamp;
}
