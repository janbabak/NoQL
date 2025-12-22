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

    /**
     * Natural language query
     */
    private String nlQuery;

    /**
     * Generated description of the result for the user
     */
    @Column(length = 1024)
    private String resultDescription;

    /**
     * Generated database language query
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

    /**
     * @return true if plotScript was generated (successfully or not)
     */
    public Boolean plotGenerated() {
        return plotScript != null && !plotScript.isEmpty();
    }

    public Boolean plotSuccessfullyGenerated() {
        return plotGenerated() != null && plotGenerationSuccess;
    }

    /**
     * @return true if dbQuery was executed (successfully or not)
     */
    public Boolean dbQueryExecuted() {
        return dbQuery != null && !dbQuery.isEmpty();
    }

    public Boolean dbQuerySuccessfullyExecuted() {
        return dbQueryExecuted() != null && dbQueryExecutionSuccess;
    }
}
