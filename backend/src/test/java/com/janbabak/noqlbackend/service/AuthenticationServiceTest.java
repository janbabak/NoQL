package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.authentication.AuthenticationFacadeInterface;
import com.janbabak.noqlbackend.dao.repository.UserRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.UserAlreadyExistsException;
import com.janbabak.noqlbackend.model.user.AuthenticationRequest;
import com.janbabak.noqlbackend.model.user.AuthenticationResponse;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.user.RegisterRequest;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    @SuppressWarnings("unused") // used in authenticationService
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    @SuppressWarnings("unused") // used in authenticationService
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthenticationFacadeInterface authenticationFacadeInterface;

    private final Authentication authentication = mock(Authentication.class);

    @Test
    @DisplayName("Register new user")
    void registerNewUser() throws UserAlreadyExistsException {
        // given
        String email = "john@gmail.com";
        RegisterRequest registerRequest = new RegisterRequest(
                "John", "Doe", email, "password");
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .password("password")
                .role(Role.ROLE_USER)
                .build();
        String accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJob256aWtAZ21haWwuY29tIiwiaWF0IjoxNzI1MTE3MjQ1LCJleHAiOjE3MjUyMDM2NDV9.4oCm9owj7de-IsYqU8KJrQVaG8WYqeeWx2jAsjPJ8wxhAltW1YkMAc9cs2R2Ckhzh7v3Vg8RhRDQor8WPW7luw";
        String refreshToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc2MDEwMzcsImV4cCI6MTcyNzYwMTE1N30.IyF9FgOzG_-6HxdJb7k-k0yY7oGoxPVtCG3MzKS0uKW-AmxTMrgN9GdaW5b0JnazJhAxsHCgV4ruxZ_GVEp-cQ";
        AuthenticationResponse authenticationResponse = new AuthenticationResponse(accessToken, refreshToken, user);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);

        // when
        AuthenticationResponse actual = authenticationService.register(registerRequest, Role.ROLE_USER);

        // then
        assertEquals(authenticationResponse, actual);
    }

    @Test
    @DisplayName("Register user already exists")
    void registerUserAlreadyExists() {
        // given
        String email = "john@gmail.com";
        RegisterRequest registerRequest = new RegisterRequest(
                "John", "Doe", email, "password");
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // then
        assertThrows(UserAlreadyExistsException.class,
                () -> authenticationService.register(registerRequest, Role.ROLE_USER));
    }

    @Test
    @DisplayName("Authenticate existing user")
    void authenticateExistingUser() throws EntityNotFoundException {
        // given
        String email = "john@gmail.com";
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(email, "password");
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .password("password")
                .role(Role.ROLE_USER)
                .build();
        String accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJob256aWtAZ21haWwuY29tIiwiaWF0IjoxNzI1MTE3MjQ1LCJleHAiOjE3MjUyMDM2NDV9.4oCm9owj7de-IsYqU8KJrQVaG8WYqeeWx2jAsjPJ8wxhAltW1YkMAc9cs2R2Ckhzh7v3Vg8RhRDQor8WPW7luw";
        String refreshToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc2MDEwMzcsImV4cCI6MTcyNzYwMTE1N30.IyF9FgOzG_-6HxdJb7k-k0yY7oGoxPVtCG3MzKS0uKW-AmxTMrgN9GdaW5b0JnazJhAxsHCgV4ruxZ_GVEp-cQ";
        AuthenticationResponse authenticationResponse = new AuthenticationResponse(accessToken, refreshToken, user);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);

        // when
        AuthenticationResponse actual = authenticationService.authenticate(authenticationRequest);

        // then
        assertEquals(authenticationResponse, actual);
    }

    @Test
    @DisplayName("Authenticate not existing user")
    void authenticateNotExistingUser() {
        // given
        String email = "john@gmail.com";
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(email, "password");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> authenticationService.authenticate(authenticationRequest));
    }

    @Test
    @DisplayName("Refresh token - valid token")
    void refreshTokenValid() throws EntityNotFoundException {
        // given
        String email = "john@gmail.com";
        String refreshToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc2MDEwMzcsImV4cCI6MTcyNzYwMTE1N30.IyF9FgOzG_-6HxdJb7k-k0yY7oGoxPVtCG3MzKS0uKW-AmxTMrgN9GdaW5b0JnazJhAxsHCgV4ruxZ_GVEp-cQ";
        String accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc2MDEwMzcsImV4cCI6MTcyNzYwMTE1N30.IyF9FgOzG_-6HxdJb7k-k0yY7oGoxPVtCG3MzKS0uKW-AmxTMrgN9GdaW5b0JnazJhAxsHCgV4ruxZ_GVEp-44";
        String newRefreshToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc2MDEwMzcsImV4cCI6MTcyNzYwMTE1N30.IyF9FgOzG_-6HxdJb7k-k0yY7oGoxPVtCG3MzKS0uKW-AmxTMrgN9GdaW5b0JnazJhAxsHCgV4ruxZ_GVEp-33";
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        when(jwtService.extractUsername(refreshToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(newRefreshToken);

        // when
        AuthenticationResponse actual = authenticationService.refreshToken(refreshToken);

        // then
        assertEquals(accessToken, actual.accessToken());
        assertEquals(newRefreshToken, actual.refreshToken());
        assertEquals(user, actual.user());
    }

    @Test
    @DisplayName("Refresh token - invalid token")
    void refreshTokenInvalid() {
        // given
        String refreshToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc2MDEwMzcsImV4cCI6MTcyNzYwMTE1N30.IyF9FgOzG_-6HxdJb7k-k0yY7oGoxPVtCG3MzKS0uKW-AmxTMrgN9GdaW5b0JnazJhAxsHCgV4ruxZ_GVEp-cQ";
        when(jwtService.extractUsername(refreshToken)).thenReturn(null);

        // then
        assertThrows(AccessDeniedException.class, () -> authenticationService.refreshToken(refreshToken));
    }

    @Test
    @DisplayName("Refresh token - token not valid for user")
    void refreshTokenTokenNotValidForUser() {
        // given
        String email = "john@gmail.com";
        String refreshToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc2MDEwMzcsImV4cCI6MTcyNzYwMTE1N30.IyF9FgOzG_-6HxdJb7k-k0yY7oGoxPVtCG3MzKS0uKW-AmxTMrgN9GdaW5b0JnazJhAxsHCgV4ruxZ_GVEp-cQ";
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        when(jwtService.extractUsername(refreshToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(false);

        // then
        assertThrows(AccessDeniedException.class, () -> authenticationService.refreshToken(refreshToken));
    }

    @Test
    @DisplayName("Refresh token - user not found")
    void refreshTokenUserNotFound() {
        // given
        String email = "john@gmail.com";
        String refreshToken = "validRefreshToken";

        when(jwtService.extractUsername(refreshToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> authenticationService.refreshToken(refreshToken));
    }

    @Test
    @DisplayName("Check if requesting self")
    void checkIfRequestingSelf() {
        // given
        UUID userId = UUID.fromString("d9223610-04b5-49e1-8b4e-7b3aeac8836a");
        String email = "john.doe@yahoo.com";
        User user = User.builder()
                .id(userId)
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .role(Role.ROLE_USER)
                .build();

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(authenticationFacadeInterface.getAuthentication()).thenReturn(authentication);

        // when
        User actual = authenticationService.checkIfRequestingSelf(userId);

        // then
        assertEquals(user, actual);
    }

    @Test
    @DisplayName("Check if requesting self fails")
    void checkIfRequestingSelfFails() {
        // given
        UUID userId = UUID.fromString("d9223610-04b5-49e1-8b4e-7b3aeac8836a");
        User user = User.builder()
                .id(userId)
                .email("john.doe@yahoo.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ROLE_USER)
                .build();

        when(authentication.getName()).thenReturn("different@email.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(authenticationFacadeInterface.getAuthentication()).thenReturn(authentication);

        // then
        assertNull(authenticationService.checkIfRequestingSelf(userId));
    }

    @Test
    @DisplayName("Check if user is admin or self request - is self request")
    void isAdminOrSelfRequestTestIsSelfRequest() {
        // given
        UUID userId = UUID.fromString("d9223610-04b5-49e1-8b4e-7b3aeac8836a");
        String email = "john.doe@yahoo.com";
        User user = User.builder()
                .id(userId)
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .role(Role.ROLE_USER)
                .build();

        when(authentication.getName()).thenReturn(email);
        when(authenticationFacadeInterface.getAuthentication()).thenReturn(authentication);
        when(authenticationFacadeInterface.isAdmin()).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        boolean actual = authenticationService.isAdminOrSelfRequest(userId);

        // then
        assertTrue(actual);
    }

    @Test
    @DisplayName("Check if user is admin or self request - is admin")
    void isAdminOrSelfRequestTestIsAdmin() {
        // given
        UUID userId = UUID.fromString("d9223610-04b5-49e1-8b4e-7b3aeac8836a");
        when(authenticationFacadeInterface.isAdmin()).thenReturn(true);

        // when
        boolean actual = authenticationService.isAdminOrSelfRequest(userId);

        // then
        assertTrue(actual);
    }

    @Test
    @DisplayName("Check if user is admin or self request - is nothing")
    void isAdminOrSelfRequestTestIsNothing() {
        // given
        UUID userId = UUID.fromString("d9223610-04b5-49e1-8b4e-7b3aeac8836a");
        String email = "john.doe@yahoo.com";
        User user = User.builder()
                .id(userId)
                .email("different@email.cz")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ROLE_USER)
                .build();

        when(authentication.getName()).thenReturn(email);
        when(authenticationFacadeInterface.getAuthentication()).thenReturn(authentication);
        when(authenticationFacadeInterface.isAdmin()).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        boolean actual = authenticationService.isAdminOrSelfRequest(userId);

        // then
        assertFalse(actual);
    }

    @Test
    @DisplayName("Check AccessDeniedExceptions thrown if user is not admin or self request - is self request")
    void ifNotAdminOrSelfRequestThrowAccessDeniedIsSelfRequest() {
        // given
        UUID userId = UUID.fromString("d9223610-04b5-49e1-8b4e-7b3aeac8836a");
        String email = "john.doe@yahoo.com";
        User user = User.builder()
                .id(userId)
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .role(Role.ROLE_USER)
                .build();

        when(authentication.getName()).thenReturn(email);
        when(authenticationFacadeInterface.getAuthentication()).thenReturn(authentication);
        when(authenticationFacadeInterface.isAdmin()).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // then
        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(userId);
    }

    @Test
    @DisplayName("Check AccessDeniedExceptions thrown if user is not admin or self request - is  admin")
    void ifNotAdminOrSelfRequestThrowAccessDeniedIsAdmin() {
        // given
        UUID userId = UUID.fromString("d9223610-04b5-49e1-8b4e-7b3aeac8836a");
        when(authenticationFacadeInterface.isAdmin()).thenReturn(true);

        // then
        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(userId);
    }

    @Test
    @DisplayName("Check AccessDeniedExceptions thrown if user is not admin or self request - is nothing")
    void ifNotAdminOrSelfRequestThrowAccessDeniedIsNothing() {
        // given
        UUID userId = UUID.fromString("d9223610-04b5-49e1-8b4e-7b3aeac8836a");
        String email = "john.doe@yahoo.com";
        User user = User.builder()
                .id(userId)
                .email("different@email.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ROLE_USER)
                .build();

        when(authentication.getName()).thenReturn(email);
        when(authenticationFacadeInterface.getAuthentication()).thenReturn(authentication);
        when(authenticationFacadeInterface.isAdmin()).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(userId));
        assertEquals("Access denied.", exception.getMessage());
    }

    @Test
    @DisplayName("Check if user is admin")
    void isAdmin() {
        when(authenticationFacadeInterface.isAdmin()).thenReturn(true);
        assertTrue(authenticationService.isAdmin());
    }

    @Test
    @DisplayName("Test that exceptions is thrown when user is not admin")
    void ifNotAdminThrowAccessDeniedThrown() {
        when(authenticationFacadeInterface.isAdmin()).thenReturn(false);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> authenticationService.ifNotAdminThrowAccessDenied());
        assertEquals("Admin ROLE required.", exception.getMessage());
    }

    @Test
    @DisplayName("Test that exceptions is not thrown when user is admin")
    void ifNotAdminThrowAccessDeniedNotThrown() {
        when(authenticationFacadeInterface.isAdmin()).thenReturn(true);
        authenticationService.ifNotAdminThrowAccessDenied();
    }
}