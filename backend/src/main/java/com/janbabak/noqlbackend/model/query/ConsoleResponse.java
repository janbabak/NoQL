package com.janbabak.noqlbackend.model.query;

import lombok.Builder;

@Builder
public record ConsoleResponse(
        RetrievedData data,
        String dbQuery,
        String error
) {

    public static ConsoleResponse failedResponse(String error) {
        return new ConsoleResponse(null, null, error);
    }
}
