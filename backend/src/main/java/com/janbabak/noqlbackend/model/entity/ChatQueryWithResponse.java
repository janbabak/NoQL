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

    /** natural language query */
    private String nlQuery;

    /** JSON in form of string<br />
     * {@code { databaseQuery: string, generatePlot: boolean, pythonCode: string }}
     */
    @Deprecated
    @Column(length = 2048)
    private String llmResponse; // TODO: remove

    /**
     * Generated description of the result for the user
     */
    @Column(length = 1024)
    private String resultDescription;

    /**
     * generated database language query
     */
    @Column(length = 512)
    private String dbQuery;

    @Column
    private Boolean dbQueryExecutionSuccess;

    @Column(length = 1024)
    private String dbExecutionErrorMessage;

    /**
     * Generated python code for plot generation
     */
    @Column(length = 2048)
    private String plotScript;

    @Column
    private Boolean plotGenerationSuccess;

    @Column(length = 1024)
    private String plotGenerationErrorMessage;

    @Column
    private Timestamp timestamp;
}
