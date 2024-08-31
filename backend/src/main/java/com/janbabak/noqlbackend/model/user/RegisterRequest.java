package com.janbabak.noqlbackend.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotNull
    @Size(min = 1, max = 40)
    private String firstName;

    @NotNull
    @Size(min = 1, max = 40)
    private String lastName;

    @NotNull
    @Email
    @Size(max = 40)
    private String email;

    @NotNull
    @Size(min = 8, max = 40)
    private String password;
}
