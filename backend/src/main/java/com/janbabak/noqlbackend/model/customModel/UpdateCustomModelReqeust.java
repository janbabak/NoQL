package com.janbabak.noqlbackend.model.customModel;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomModelReqeust {

    @Nullable
    @Length(min = 1, max = 32)
    private String name;

    @Nullable
    private String host;

    @Nullable
    @Min(value = 1)
    private Integer port;
}
