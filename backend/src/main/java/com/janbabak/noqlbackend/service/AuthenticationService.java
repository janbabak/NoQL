package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.authentication.AuthenticationFacadeInterface;
import com.janbabak.noqlbackend.dao.repository.UserRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.UserAlreadyExistsException;
import com.janbabak.noqlbackend.model.AuthenticationRequest;
import com.janbabak.noqlbackend.model.AuthenticationResponse;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.user.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.USER;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationFacadeInterface authenticationFacadeInterface;

    /**
     * Register new user, create new user.
     * @param request user data
     * @return response with new user data and JWT token
     * @throws UserAlreadyExistsException User with this username already exists.
     */
    public AuthenticationResponse register(RegisterRequest request) throws UserAlreadyExistsException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = userRepository.save(new User(request, passwordEncoder));
        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .user(user)
                .build();
    }

    /**
     * Authenticate existing user.
     * @param request request with username and password
     * @return response with user data and JWT token
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .user(user)
                .build();
    }

    /**
     * Check if id corresponds to authenticated user.
     * @param id id to check
     * @return user if id is same as id of authenticated user, otherwise null
     * @throws EntityNotFoundException User of specified id doesn't exist.
     */
    public User checkIfRequestingSelf(UUID id) throws EntityNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(USER, id));

        if (authenticationFacadeInterface.getAuthentication() == null
                || !user.getEmail().equals(authenticationFacadeInterface.getAuthentication().getName())) {
            throw new AccessDeniedException("Access denied.");
        }
        return user;
    }

    /**
     * Check if authenticated user has role ADMIN or id corresponds to authenticate user.
     * @param id id to check
     * @return true if is ADMIN or id is same as id of authenticated user
     * @throws EntityNotFoundException User of specified id doesn't exist.
     */
    public boolean isNotAdminOrSelfRequest(UUID id) throws EntityNotFoundException {
        return !authenticationFacadeInterface.isAdmin() && checkIfRequestingSelf(id) == null;
    }

    /**
     * If authenticated user hasn't role ADMIN and id doesn't correspond to id, throw Access denied exception.
     * @param id id to check
     * @throws EntityNotFoundException User of specified id doesn't exist.
     */
    public void ifNotAdminOrSelfRequestThrowAccessDenied(UUID id) throws EntityNotFoundException {
        if (isNotAdminOrSelfRequest(id)) {
            throw new AccessDeniedException("Access denied.");
        }
    }

    /**
     * Return if authenticated user has role ADMIN.
     * @return true if he/she has, otherwise false
     */
    public boolean isAdmin() {
        return authenticationFacadeInterface.isAdmin();
    }

    /**
     * If authenticated user hasn't role ADMIN, throw Access denied exception.
     */
    public void ifNotAdminThrowAccessDenied() {
        if (!isAdmin()) {
            throw new AccessDeniedException("Admin ROLE required.");
        }
    }
}
