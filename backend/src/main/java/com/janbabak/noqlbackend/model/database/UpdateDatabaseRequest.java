package com.janbabak.noqlbackend.model.database;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDatabaseRequest {

    @Nullable
    @Size(min = 1, max = 32)
    private String name;

    private String host;

    @Nullable
    @Min(1)
    private Integer port;

    private String database;

    private String userName;

    private String password;

    private DatabaseEngine engine;

    private Boolean isSQL;
}
