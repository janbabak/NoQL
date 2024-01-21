package com.janbabak.noqlbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserQueryRequest {
    private String databaseId;
    private String naturalLanguageQuery;
}
