package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.service.JwtService;
import com.janbabak.noqlbackend.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.USER;
import static com.janbabak.noqlbackend.service.utils.JsonUtils.toJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(JwtService.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private final String ROOT_URL = "/user";

    private final User testUser = User.builder()
            .id(UUID.fromString("af11c153-2948-4922-bca7-3e407a40da02"))
            .firstName("Test")
            .build();

    @Test
    @DisplayName("Get all users")
    @WithMockUser("ADMIN")
    void getAll() throws Exception {
        // given
        User user1 = User.builder()
                .id(UUID.fromString("cf11c153-2948-4922-bca7-3e407a40da02"))
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@email.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        User user2 = User.builder()
                .id(UUID.fromString("df11c153-2948-4922-bca7-3e407a40da02"))
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@email.com")
                .password("password2")
                .role(Role.ROLE_ADMIN)
                .build();

        List<User> users = List.of(user1, user2);

        when(userService.findAll()).thenReturn(users);

        // then
        mockMvc.perform(get(ROOT_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(users), true));
    }

    @Test
    @DisplayName("Get user by id")
    @WithMockUser(roles = "USER")
    void getById() throws Exception {

        // given
        when(userService.findById(testUser.getId())).thenReturn(testUser);

        // then
        mockMvc.perform(get(ROOT_URL + "/{userId}", testUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(testUser), true));
    }

    @Test
    @DisplayName("Test find user by id - user not found")
    @WithMockUser(roles = "USER")
    void getByIdUserNotFound() throws Exception {
        // given
        when(userService.findById(testUser.getId())).thenThrow(new EntityNotFoundException(USER, testUser.getId()));

        // then
        mockMvc.perform(get(ROOT_URL + "/{userId}", testUser.getId()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @DisplayName("Update user")
    @MethodSource("updateDatabaseDataProvider")
    @WithMockUser(roles = "USER")
    void updateUser(String request, User updatedUser, String response, Boolean success) throws Exception {
        // given
        UUID userId = UUID.fromString("af11c153-2948-4922-bca7-3e407a40da02");

        if (success) {
            when(userService.updateUser(eq(userId), any())).thenReturn(updatedUser);
        }

        // then
        mockMvc.perform(put(ROOT_URL + "/{userId}", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
                        .with(csrf()))
                .andDo(print())
                .andExpect(success ? status().isOk() : status().isBadRequest())
                .andExpect(content().json(response, true));
    }

    Object[][] updateDatabaseDataProvider() {
        return new Object[][] {
                {
                        // language=JSON
                        """
                        {
                            "firstName": "updated name"
                        }""",
                        User.builder()
                                .id(UUID.fromString("af11c153-2948-4922-bca7-3e407a40da02"))
                                .firstName("updated name")
                                .lastName("Doe")
                                .email("john.doe@email.com")
                                .password("password")
                                .role(Role.ROLE_USER)
                                .build(),
                        // language=JSON
                        """
                        {
                            "id": "af11c153-2948-4922-bca7-3e407a40da02",
                            "firstName": "updated name",
                            "lastName": "Doe",
                            "email": "john.doe@email.com",
                            "role": "USER"
                        }""",
                        true
                },
                {
                        // language=JSON
                        """
                        {
                            "firstName": "updated name",
                            "lastName": "updated last name",
                            "email": "john.updated@email.com",
                            "role": "ADMIN"
                        }""",
                        User.builder()
                                .id(UUID.fromString("af11c153-2948-4922-bca7-3e407a40da02"))
                                .firstName("updated name")
                                .lastName("updated last name")
                                .email("john.updated@email.com")
                                .password("updated password")
                                .role(Role.ROLE_ADMIN)
                                .build(),
                        // language=JSON
                        """
                        {
                            "id": "af11c153-2948-4922-bca7-3e407a40da02",
                            "firstName": "updated name",
                            "lastName": "updated last name",
                            "email": "john.updated@email.com",
                            "role": "ADMIN"
                        }""",
                        true
                },
                {
                        // language=JSON
                        """
                        {
                            "firstName": "",
                            "lastName": "",
                            "email": "",
                            "password": ""
                        }""",
                        null,
                        // language=JSON
                        """
                        {
                             "firstName": "size must be between 1 and 32",
                             "lastName": "size must be between 1 and 32",
                             "password": "size must be between 8 and 64"
                         }""",
                        false
                },
                {
                        // language=JSON
                        """
                        {
                            "firstName": "too long name too long name too long name too long name",
                            "lastName": "too long name too long name too long name too long name",
                            "email": "wrong format",
                            "password": "short"
                        }""",
                        null,
                        // language=JSON
                        """
                        {
                            "firstName": "size must be between 1 and 32",
                            "lastName": "size must be between 1 and 32",
                            "password": "size must be between 8 and 64",
                            "email": "must be a well-formed email address"
                        }""",
                        false
                }
        };
    }

    @Test
    @DisplayName("Delete user")
    @WithMockUser(roles = "USER")
    void deleteUser() throws Exception {
        // then
        mockMvc.perform(delete(ROOT_URL + "/{userId}", testUser.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}