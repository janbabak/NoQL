package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.user.UpdateUserRequest;
import com.janbabak.noqlbackend.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get all users.
     *
     * @return list of users
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or self request
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAll() {
        return userService.findAll();
    }

    /**
     * Get user by id.
     *
     * @param userId user identifier
     * @return user
     * @throws EntityNotFoundException                                   user of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin
     */
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public User getById(@PathVariable UUID userId) throws EntityNotFoundException {
        return userService.findById(userId);
    }

    /**
     * Update user data.
     *
     * @param userId  user identifier
     * @param request user data
     * @return updated user
     * @throws EntityNotFoundException                                   user of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or self request,
     *                                                                   or if role USER tries to set role ADMIN
     */
    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@PathVariable UUID userId, @RequestBody @Valid UpdateUserRequest request)
            throws EntityNotFoundException {

        return userService.updateUser(userId, request);
    }

    /**
     * Delete user by id.
     *
     * @param userId user identifier
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or self request
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
    }
}
