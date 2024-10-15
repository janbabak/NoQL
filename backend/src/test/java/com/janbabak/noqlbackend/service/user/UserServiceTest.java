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
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    @SuppressWarnings("unused") // used in userService
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Find user by id")
    void testFindUserById() throws EntityNotFoundException {
        // given
        UUID userId = UUID.randomUUID();

        User user = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

        // when
        User actual = userService.findById(userId);

        // then
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(userRepository).findById(idCaptor.capture());
        assertEquals(userId, idCaptor.getValue());
        assertEquals(user, actual);
    }

    @Test
    @DisplayName("Find user by id - user not found")
    void testFindUserByIdUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.findById(userId));

        // then
        assertEquals("User of id: \"" + userId + "\" not found.", exception.getMessage()); // TODO: check in other tests
    }

    @Test
    @DisplayName("Find user by id - access denied")
    void testFindUserByIdAccessDenied() {
        // given
        UUID userId = UUID.randomUUID();

        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationService).ifNotAdminOrSelfRequestThrowAccessDenied(userId);

        // then
        assertThrows(AccessDeniedException.class, () -> userService.findById(userId));
    }

    @Test
    @DisplayName("Find all users")
    void testFindAllUsers() {
        // given
        User user1 = User.builder().id(UUID.randomUUID()).build();
        User user2 = User.builder().id(UUID.randomUUID()).build();
        List<User> users = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        // when
        List<User> actual = userService.findAll();

        // then
        assertEquals(2, actual.size());
        assertEquals(users, actual);
    }

    @Test
    @DisplayName("Update user")
    void testUpdateUser() throws EntityNotFoundException {
        // given
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .firstName("john")
                .lastName("doe")
                .email("john.doe@email.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com")
                .password("password2")
                .queryLimit(1000)
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com")
                .password(passwordEncoder.encode("password2"))
                .role(Role.ROLE_USER)
                .queryLimit(1000)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(updatedUser);

        // when
        User actual = userService.updateUser(userId, request);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(updatedUser, userCaptor.getValue());
        assertEquals(updatedUser, actual);
    }

    @Test
    @DisplayName("Update user - user not found")
    void testUpdateUserUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();

        UpdateUserRequest request = UpdateUserRequest.builder().firstName("new name").build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(userId, request));

        // then
        assertEquals("User of id: \"" + userId + "\" not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Find user by id - access denied")
    void testUpdateUserByIdAccessDenied() {
        // given
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = UpdateUserRequest.builder().firstName("new name").build();

        doThrow(new AccessDeniedException("Access Denied"))
                .when(authenticationService).ifNotAdminOrSelfRequestThrowAccessDenied(userId);

        // then
        assertThrows(AccessDeniedException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    @DisplayName("Delete user")
    void testDeleteUser() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userService.deleteUser(userId);

        // then
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(userRepository).deleteById(idCaptor.capture());
        assertEquals(userId, idCaptor.getValue());
    }

    @Test
    @DisplayName("Decrement query limit")
    void testDecrementQueryLimit() throws EntityNotFoundException {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).queryLimit(10).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        int actual = userService.decrementQueryLimit(userId);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(9, userCaptor.getValue().getQueryLimit());
        assertEquals(actual, 10);
    }

    @Test
    @DisplayName("Decrement query limit - limit exceeded")
    void testDecrementQueryLimitExceeded() throws EntityNotFoundException {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).queryLimit(0).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        int actual = userService.decrementQueryLimit(userId);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(0, userCaptor.getValue().getQueryLimit());
        assertEquals(actual, 0);
    }

    @Test
    @DisplayName("Decrement query limit - user not found")
    void testDecrementQueryLimitUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // then
        assertThrows(EntityNotFoundException.class, () -> userService.decrementQueryLimit(userId));
    }
}