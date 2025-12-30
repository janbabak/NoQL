package com.janbabak.noqlbackend.service.user;

import com.janbabak.noqlbackend.dao.repository.UserRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.user.UpdateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private PasswordEncoder passwordEncoderMock;

    @Mock
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Find user by id")
    void testFindUserById() throws EntityNotFoundException {
        // given
        final UUID userId = UUID.randomUUID();

        final User user = User.builder().id(userId).build();

        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(user));

        // when
        final User actual = userService.findById(userId);

        // then
        final ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(userRepositoryMock).findById(idCaptor.capture());
        assertEquals(userId, idCaptor.getValue());
        assertEquals(user, actual);
    }

    @Test
    @DisplayName("Find user by id - user not found")
    void testFindUserByIdUserNotFound() {
        // given
        final UUID userId = UUID.randomUUID();

        when(userRepositoryMock.findById(userId)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.findById(userId));

        // then
        assertEquals("User of id: \"" + userId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Find user by id - access denied")
    void testFindUserByIdAccessDenied() {
        // given
        final UUID userId = UUID.randomUUID();

        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationService).ifNotAdminOrSelfRequestThrowAccessDenied(userId);

        // then
        assertThrows(AccessDeniedException.class, () -> userService.findById(userId));
    }

    @Test
    @DisplayName("Find all users")
    void testFindAllUsers() {
        // given
        final User user1 = User.builder().id(UUID.randomUUID()).build();
        final User user2 = User.builder().id(UUID.randomUUID()).build();
        final List<User> users = List.of(user1, user2);

        when(userRepositoryMock.findAll()).thenReturn(users);

        // when
        final List<User> actual = userService.findAll();

        // then
        assertEquals(2, actual.size());
        assertEquals(users, actual);
    }

    @Test
    @DisplayName("Update user")
    void testUpdateUser() throws EntityNotFoundException {
        // given
        final UUID userId = UUID.randomUUID();

        final User user = User.builder()
                .id(userId)
                .firstName("john")
                .lastName("doe")
                .email("john.doe@email.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        final UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com")
                .password("password2")
                .queryLimit(1000)
                .build();

        final User updatedUser = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com")
                .password(passwordEncoderMock.encode("password2"))
                .role(Role.ROLE_USER)
                .queryLimit(1000)
                .build();

        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(user));
        when(userRepositoryMock.save(user)).thenReturn(updatedUser);

        // when
        final User actual = userService.updateUser(userId, request);

        // then
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryMock).save(userCaptor.capture());
        assertEquals(updatedUser, userCaptor.getValue());
        assertEquals(updatedUser, actual);
    }

    @Test
    @DisplayName("Update user - email already taken")
    void testUpdateUserEmailTaken() {
        // given
        final UUID userId = UUID.randomUUID();

        final User user = User.builder()
                .id(userId)
                .firstName("john")
                .lastName("doe")
                .email("john.doe@email.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        final UpdateUserRequest request = UpdateUserRequest.builder()
                .email("john@email.com")
                .build();

        final User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("john@email.com")
                .build();

        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(user));
        when(userRepositoryMock.findByEmail(eq("john@email.com"))).thenReturn(Optional.of(existingUser));

        // when
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userId, request));

        // then
        assertEquals("User with this email already exists.", exception.getMessage());
    }

    @Test
    @DisplayName("Update user - user not found")
    void testUpdateUserUserNotFound() {
        // given
        final UUID userId = UUID.randomUUID();

        final UpdateUserRequest request = UpdateUserRequest.builder().firstName("new name").build();

        when(userRepositoryMock.findById(userId)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(userId, request));

        // then
        assertEquals("User of id: \"" + userId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Find user by id - access denied")
    void testUpdateUserByIdAccessDenied() {
        // given
        final UUID userId = UUID.randomUUID();
        final UpdateUserRequest request = UpdateUserRequest.builder().firstName("new name").build();

        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationService).ifNotAdminOrSelfRequestThrowAccessDenied(userId);

        // then
        assertThrows(AccessDeniedException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    @DisplayName("Delete user")
    void testDeleteUser() {
        // given
        final UUID userId = UUID.randomUUID();
        final User user = User.builder().id(userId).build();

        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(user));

        // when
        userService.deleteUser(userId);

        // then
        final ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(userRepositoryMock).deleteById(idCaptor.capture());
        assertEquals(userId, idCaptor.getValue());
    }

    @Test
    @DisplayName("Decrement query limit")
    void testDecrementQueryLimit() throws EntityNotFoundException {
        // given
        final UUID userId = UUID.randomUUID();
        final User user = User.builder().id(userId).queryLimit(10).build();

        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(user));

        // when
        final int actual = userService.decrementQueryLimit(userId);

        // then
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryMock).save(userCaptor.capture());
        assertEquals(9, userCaptor.getValue().getQueryLimit());
        assertEquals(10, actual);
    }

    @Test
    @DisplayName("Decrement query limit - limit exceeded")
    void testDecrementQueryLimitExceeded() throws EntityNotFoundException {
        // given
        final UUID userId = UUID.randomUUID();
        final User user = User.builder().id(userId).queryLimit(0).build();

        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(user));

        // when
        final int actual = userService.decrementQueryLimit(userId);

        // then
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryMock).save(userCaptor.capture());
        assertEquals(0, userCaptor.getValue().getQueryLimit());
        assertEquals(0, actual);
    }

    @Test
    @DisplayName("Decrement query limit - user not found")
    void testDecrementQueryLimitUserNotFound() {
        // given
        final UUID userId = UUID.randomUUID();
        when(userRepositoryMock.findById(userId)).thenReturn(Optional.empty());

        // when
        final EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.decrementQueryLimit(userId));

        // then
        assertEquals("User of id: \"" + userId + "\" not found.", exception.getMessage());
    }
}