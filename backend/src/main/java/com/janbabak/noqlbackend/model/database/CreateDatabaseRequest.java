package com.janbabak.noqlbackend.model.database;

import com.janbabak.noqlbackend.validation.FirstValidationGroup;
import com.janbabak.noqlbackend.validation.SecondValidationGroup;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateDatabaseRequest {

    /**
     * User-defined name of the database.
     */
    @NotBlank(groups = FirstValidationGroup.class)
    @Length(min = 1, max = 32, groups = SecondValidationGroup.class)
    private String name;

    @NotBlank(groups = FirstValidationGroup.class)
    private String host;

    @NotNull(groups = FirstValidationGroup.class)
    @Min(value = 1, groups = SecondValidationGroup.class)
    private Integer port;

    /**
     * Name of the database to connect to (used in the connection URL).
     */
    @NotBlank(groups = FirstValidationGroup.class)
    @Length(min = 1, max = 253, groups = SecondValidationGroup.class)
    private String database;

    @NotBlank(groups = FirstValidationGroup.class)
    @Length(min = 1, max = 128, groups = SecondValidationGroup.class)
    private String userName;

    @NotBlank(groups = FirstValidationGroup.class)
    @Length(min = 1, max = 253, groups = SecondValidationGroup.class)
    private String password;

    @NotNull(groups = FirstValidationGroup.class)
    private DatabaseEngine engine;

    @NotNull(groups = FirstValidationGroup.class)
    private UUID userId;
}
