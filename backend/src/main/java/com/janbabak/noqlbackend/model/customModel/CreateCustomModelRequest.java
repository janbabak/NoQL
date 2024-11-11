package com.janbabak.noqlbackend.model.customModel;

import com.janbabak.noqlbackend.validation.FirstValidationGroup;
import com.janbabak.noqlbackend.validation.SecondValidationGroup;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Builder
public record CreateCustomModelRequest(
        @NotBlank(groups = FirstValidationGroup.class)
        @Length(min = 1, max = 32, groups = SecondValidationGroup.class)
        String name,

        @NotBlank(groups = FirstValidationGroup.class)
        @Length(min = 1, max = 253, groups = SecondValidationGroup.class)
        String host,

        @NotNull(groups = FirstValidationGroup.class)
        @Min(value = 1, groups = SecondValidationGroup.class)
        Integer port,

        @NotNull(groups = FirstValidationGroup.class)
        UUID userId
) {
}
