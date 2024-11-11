package com.janbabak.noqlbackend.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterRequest(
        @NotNull
        @Size(min = 1, max = 32)
        String firstName,

        @NotNull
        @Size(min = 1, max = 32)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 64)
        String email,

        @NotNull
        @Size(min = 8, max = 64)
        String password) {
}