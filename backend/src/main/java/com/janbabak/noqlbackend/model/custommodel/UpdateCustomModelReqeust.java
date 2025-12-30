package com.janbabak.noqlbackend.model.custommodel;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Deprecated
@Builder
public record UpdateCustomModelReqeust(
        @Nullable
        @Length(min = 1, max = 32)
        String name,

        @Nullable
        @Length(min = 1, max = 253)
        String host,

        @Nullable
        @Min(1)
        Integer port
) {
}
