package com.janbabak.noqlbackend.model.user;

import com.janbabak.noqlbackend.model.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String token,
        String refreshToken,
        @NotNull User user) {
}
