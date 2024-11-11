package com.janbabak.noqlbackend.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AuthenticationRequest(
        @NotBlank
        @Email
        String email,

        @NotNull
        String password) {
}
