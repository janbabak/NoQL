package com.janbabak.noqlbackend.service.user;

import com.janbabak.noqlbackend.dao.repository.UserRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.user.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.USER;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Find user by id.
     *
     * @param userId user identifier
     * @return user
     * @throws EntityNotFoundException                                   user of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or self request
     */
    public User findById(UUID userId) throws EntityNotFoundException {
        log.info("Get user by id={}.", userId);

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(userId);

        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER, userId));
    }

    /**
     * Find all users.
     *
     * @return list of users
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin
     */
    public List<User> findAll() {
        log.info("Get all users.");

        authenticationService.ifNotAdminThrowAccessDenied();

        return userRepository.findAll();
    }

    /**
     * Update user data.
     *
     * @param userId user identifier
     * @param data   new user data
     * @return updated user
     * @throws EntityNotFoundException user of specified id not found.
     */
    public User updateUser(UUID userId, UpdateUserRequest data) throws EntityNotFoundException {
        log.info("Update user by id={}.", userId);

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(userId);

        User userToUpdate = findById(userId);

        if (data.firstName() != null) userToUpdate.setFirstName(data.firstName());
        if (data.lastName() != null) userToUpdate.setLastName(data.lastName());
        if (data.queryLimit() != null) userToUpdate.setQueryLimit(data.queryLimit());
        if (data.password() != null) userToUpdate.setPassword(passwordEncoder.encode(data.password()));
        if (data.email() != null && !data.email().isEmpty()) {
            userRepository.findByEmail(data.email()).ifPresent(user -> {
                if (!user.getId().equals(userId)) {
                    throw new IllegalArgumentException("User with this email already exists.");
                }
            });
            userToUpdate.setEmail(data.email());
        }
        if (data.role() != null) {
            if (data.role() == Role.ROLE_ADMIN) {
                // only admin is allowed to change role to admin
                authenticationService.ifNotAdminThrowAccessDenied();
            }
            userToUpdate.setRole(data.role());
        }

        return userRepository.save(userToUpdate);
    }

    /**
     * Delete user by id.
     *
     * @param userId user identifier
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or self request
     */
    public void deleteUser(UUID userId) {
        log.info("Delete user by id={}.", userId);

        Optional<User> user = userRepository.findById(userId);

        if (user.isPresent()) {
            authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(userId);
            userRepository.deleteById(userId);
        }
    }


    /**
     * Decrement query limit for user.
     *
     * @param userId user identifier
     * @return new query limit
     * @throws EntityNotFoundException user of specified id not found.
     */
    public Integer decrementQueryLimit(UUID userId) throws EntityNotFoundException {
        log.info("Decrement query limit for user with id={}.", userId);

        User user = findById(userId);
        int odlLimit = user.getQueryLimit();
        user.setQueryLimit(Math.max(odlLimit - 1, 0));
        userRepository.save(user);
        return odlLimit;
    }
}
