package com.janbabak.noqlbackend.model.user;

import com.janbabak.noqlbackend.model.Role;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @Nullable
    @Column(unique = true)
    private String email;

    @Nullable
    private String password;

    @Nullable
    @Enumerated(EnumType.STRING)
    private Role role;
}
