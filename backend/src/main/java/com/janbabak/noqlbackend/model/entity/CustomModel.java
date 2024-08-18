package com.janbabak.noqlbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.janbabak.noqlbackend.validation.FirstValidationGroup;
import com.janbabak.noqlbackend.validation.SecondValidationGroup;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

/**
 * Custom LLM that can be plugged via API.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomModel {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank(groups = FirstValidationGroup.class)
    @Length(min = 1, max = 32, groups = SecondValidationGroup.class)
    private String name;

    @NotBlank(groups = FirstValidationGroup.class)
    @Length(min = 1, max = 253, groups = SecondValidationGroup.class)
    private String host;

    @NotNull(groups = FirstValidationGroup.class)
    @Min(value = 1, groups = SecondValidationGroup.class)
    private Integer port;

    @JsonIgnore
    public String getUrl() {
        return host + ":" + port;
    }
}
