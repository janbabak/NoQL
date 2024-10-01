package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.UserAlreadyExistsException;
import com.janbabak.noqlbackend.model.user.AuthenticationRequest;
import com.janbabak.noqlbackend.model.user.AuthenticationResponse;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.user.RegisterRequest;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Authenticate existing user.
     *
     * @param request request with username and password
     * @return response with user data and auth tokens
     * @throws EntityNotFoundException User with this username not found.
     */
    @PostMapping("/authenticate")
    @ResponseStatus(HttpStatus.OK)
    public AuthenticationResponse authenticate(@RequestBody @Valid AuthenticationRequest request)
            throws EntityNotFoundException {
        return authenticationService.authenticate(request);
    }

    /**
     * Register new user, create new user with USER role.
     *
     * @param request user data
     * @return response with new user data and auth tokens
     * @throws UserAlreadyExistsException User with this username already exists.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthenticationResponse register(@RequestBody @Valid RegisterRequest request)
            throws UserAlreadyExistsException {
        return authenticationService.register(request, Role.ROLE_USER);
    }

    /**
     * Refresh access and refresh token.
     *
     * @param refreshToken refreshToken
     * @return response with new access and refresh token
     * @throws EntityNotFoundException user not found.
     */
    @PostMapping("/refreshToken")
    public AuthenticationResponse refreshToken(@RequestBody String refreshToken) throws EntityNotFoundException {
        return authenticationService.refreshToken(refreshToken);
    }
}
