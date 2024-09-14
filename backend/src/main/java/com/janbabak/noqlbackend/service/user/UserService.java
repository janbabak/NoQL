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
     * @throws EntityNotFoundException user of specified id not found.
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
     * @param userId user identifier
     * @param data new user data
     * @return updated user
     * @throws EntityNotFoundException user of specified id not found.
     */
    public User updateUser(UUID userId, UpdateUserRequest data) throws EntityNotFoundException {
        log.info("Update user by id={}.", userId);

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(userId);

        User userToUpdate = findById(userId);

        if (data.getFirstName() != null) userToUpdate.setFirstName(data.getFirstName());
        if (data.getLastName() != null) userToUpdate.setLastName(data.getLastName());
        if (data.getPassword() != null) userToUpdate.setPassword(passwordEncoder.encode(data.getPassword()));
        if (data.getEmail() != null && !data.getEmail().isEmpty()) {
            userRepository.findByEmail(data.getEmail()).ifPresent(user -> {
                if (!user.getId().equals(userId)) {
                    throw new IllegalArgumentException("User with this email already exists.");
                }
            });
            userToUpdate.setEmail(data.getEmail());
        }
        if (data.getRole() != null) {
            if (data.getRole() == Role.ADMIN) {
                // only admin is allowed to change role to admin
                authenticationService.ifNotAdminThrowAccessDenied();
            }
            userToUpdate.setRole(data.getRole());
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
}
