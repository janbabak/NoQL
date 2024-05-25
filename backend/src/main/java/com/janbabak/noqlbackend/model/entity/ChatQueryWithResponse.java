package com.janbabak.noqlbackend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    /** natural language query */
    @NotBlank
    private String nlQuery;

    /** JSON in form of string<br />
     * { {@code  databaseQuery: string, generatePlot: boolean, pythonCode: string } }
     */
    @Column(length = 2048)
    private String llmResponse;

    private Timestamp timestamp;
}
