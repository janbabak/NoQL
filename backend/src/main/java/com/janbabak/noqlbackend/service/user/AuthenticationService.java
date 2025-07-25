package com.janbabak.noqlbackend.service.user;

import com.janbabak.noqlbackend.authentication.AuthenticationFacadeInterface;
import com.janbabak.noqlbackend.dao.repository.UserRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.UserAlreadyExistsException;
import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.user.AuthenticationRequest;
import com.janbabak.noqlbackend.model.user.AuthenticationResponse;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.user.RegisterRequest;
import com.janbabak.noqlbackend.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.USER;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationFacadeInterface authenticationFacadeInterface;
    private final Settings settings;

    /**
     * Register new user, create new user.
     *
     * @param request user data
     * @param role    role of new user
     * @return response with new user data and auth tokens
     * @throws UserAlreadyExistsException User with this username already exists.
     */
    public AuthenticationResponse register(RegisterRequest request, Role role) throws UserAlreadyExistsException {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException(request.email());
        }

        log.info("Created new user with default query limit: {}", settings.getDefaultUserQueryLimit());

        User user = userRepository.save(new User(request, passwordEncoder, role, settings.getDefaultUserQueryLimit()));
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .user(user)
                .build();
    }

    /**
     * Authenticate existing user.
     *
     * @param request request with username and password
     * @return response with user data and auth tokens
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) throws EntityNotFoundException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email()).orElseThrow(
                () -> new EntityNotFoundException(USER, request.email()));

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .user(user)
                .build();
    }

    /**
     * Refresh access and refresh token.
     *
     * @param refreshToken refresh token
     * @return response with new access and refresh token
     * @throws EntityNotFoundException user not found.
     */
    public AuthenticationResponse refreshToken(String refreshToken) throws EntityNotFoundException {
        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElseThrow(
                    () -> new EntityNotFoundException(USER, userEmail));

            if (jwtService.isTokenValid(refreshToken, user)) {
                String accessToken = jwtService.generateToken(user);
                String newRefreshToken = jwtService.generateRefreshToken(user);
                return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(newRefreshToken)
                        .user(user)
                        .build();
            }
        }
        throw new AccessDeniedException("Access denied.");
    }

    /**
     * Check if id corresponds to authenticated user.
     *
     * @param id id to check
     * @return user if id is same as id of authenticated user, otherwise null
     */
    public User checkIfRequestingSelf(UUID id) {
        if (id == null) {
            return null;
        }

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()
                || authenticationFacadeInterface.getAuthentication() == null
                || !user.get().getEmail().equals(authenticationFacadeInterface.getAuthentication().getName())) {
            return null;
        }
        return user.get();
    }

    /**
     * Check if authenticated user has role ADMIN or id corresponds to authenticate user.
     *
     * @param id id to check
     * @return true if is ADMIN or id is same as id of authenticated user
     */
    public boolean isAdminOrSelfRequest(UUID id) {
        return authenticationFacadeInterface.isAdmin() || checkIfRequestingSelf(id) != null;
    }

    /**
     * If authenticated user hasn't role ADMIN and id that doesn't correspond to id, throw Access denied exception.
     *
     * @param id id to compare with user's id
     */
    public void ifNotAdminOrSelfRequestThrowAccessDenied(UUID id) {
        if (!isAdminOrSelfRequest(id)) {
            throw new AccessDeniedException("Access denied.");
        }
    }

    /**
     * Check if authenticated user has role ADMIN.
     *
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

    /**
     * Authenticate user to the spring security context.
     *
     * @param user user to authenticate
     */
    public static void authenticateUser(User user) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
