package com.janbabak.noqlbackend.model.user;

import com.janbabak.noqlbackend.model.Role;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateUserRequest(

        @Nullable
        @Size(min = 1, max = 32)
        String firstName,

        @Nullable
        @Size(min = 1, max = 32)
        String lastName,

        @Nullable
        @Email
        String email,

        @Nullable
        @Size(min = 8, max = 64)
        String password,

        @Nullable
        Role role,

        @Nullable
        @Min(0)
        Integer queryLimit) { // TODO: easily hackable
}
