package com.janbabak.noqlbackend.model.database;

import com.janbabak.noqlbackend.validation.ValidHostName;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDatabaseRequest {

    /**
     * User-defined name of the database.
     */
    @Nullable
    @Length(min = 1, max = 32)
    private String name;

    @Nullable
    @ValidHostName
    private String host;

    @Nullable
    @Min(1)
    private Integer port;

    /**
     * Name of the database to connect to (used in the connection URL).
     */
    @Nullable
    @Length(min = 1, max = 253)
    private String database;

    @Nullable
    @Length(min = 1, max = 128)
    private String userName;

    @Nullable
    @Length(min = 1, max = 253)
    private String password;

    @Nullable
    private DatabaseEngine engine;
}
