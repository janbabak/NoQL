package com.janbabak.noqlbackend.model.user;

import com.janbabak.noqlbackend.model.Role;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserRequest {

    @Nullable
    @Size(min = 1, max = 32)
    private String firstName;

    @Nullable
    @Size(min = 1, max = 32)
    private String lastName;

    @Nullable
    @Email
    private String email;

    @Nullable
    @Size(min = 8, max = 64)
    private String password;

    @Nullable
    private Role role;

    @Nullable
    @Min(0)
    private Integer queryLimit; // TODO: easily hackable
}
