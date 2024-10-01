package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.model.user.AuthenticationRequest;
import com.janbabak.noqlbackend.model.user.AuthenticationResponse;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.user.RegisterRequest;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import com.janbabak.noqlbackend.service.JwtService;
import com.janbabak.noqlbackend.service.utils.JsonUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(JwtService.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    private final String ROOT_URL = "/auth";

    @ParameterizedTest
    @DisplayName("Authenticate user")
    @MethodSource("authenticateDataProvider")
    @WithAnonymousUser
    void authenticate(String requestJson,
                       AuthenticationRequest requestObj,
                       String responseJson,
                       AuthenticationResponse responseObj,
                       Boolean success) throws Exception {

        if (success) {
            when(authenticationService.authenticate(requestObj)).thenReturn(responseObj);
        }

        // then
        mockMvc.perform(post(ROOT_URL + "/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andDo(print())
                .andExpect(success ? status().isOk() : status().isBadRequest())
                .andExpect(content().json(responseJson, true));
    }

    static Object[][] authenticateDataProvider() {
        return new Object[][]{
                {
                        // language=JSON
                        """
                        {
                            "email": "john.doe@gmail.com",
                            "password": "40580jkdjfJIJj"
                        }""",
                        AuthenticationRequest.builder()
                                .email("john.doe@gmail.com")
                                .password("40580jkdjfJIJj")
                                .build(),
                        // language=JSON
                        """
                        {
                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJleHAiOjE2ODU4NzQ3MzksImlhdCI6MTY4NTc4ODMzOSwiYXV0aG9yaXRpZXMiOlt7ImF1dGhvcml0eSI6IkFETUlOIn1dfQ.G4axFV8xRkzcpLulzPXp-bRuRc3lQIs2vp_jkb6LxLw",
                            "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc2MDEwMzcsImV4cCI6MTcyNzYwMTE1N30.IyF9FgOzG_-6HxdJb7k-k0yY7oGoxPVtCG3MzKS0uKW-AmxTMrgN9GdaW5b0JnazJhAxsHCgV4ruxZ_GVEp-cQ",
                            "user": {
                                "id": "af11c153-2948-4922-bca7-3e407a40da02",
                                "firstName": "John",
                                "lastName": "Doe",
                                "email": "john.doe@gmail.com",
                                "role": "ROLE_USER"
                            }
                        }""",
                        AuthenticationResponse.builder()
                                .accessToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJleHAiOjE2ODU4NzQ3MzksImlhdCI6MTY4NTc4ODMzOSwiYXV0aG9yaXRpZXMiOlt7ImF1dGhvcml0eSI6IkFETUlOIn1dfQ.G4axFV8xRkzcpLulzPXp-bRuRc3lQIs2vp_jkb6LxLw")
                                .refreshToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc2MDEwMzcsImV4cCI6MTcyNzYwMTE1N30.IyF9FgOzG_-6HxdJb7k-k0yY7oGoxPVtCG3MzKS0uKW-AmxTMrgN9GdaW5b0JnazJhAxsHCgV4ruxZ_GVEp-cQ")
                                .user(User.builder()
                                        .id(UUID.fromString("af11c153-2948-4922-bca7-3e407a40da02"))
                                        .firstName("John")
                                        .lastName("Doe")
                                        .email("john.doe@gmail.com")
                                        .role(Role.ROLE_USER)
                                        .databases(new ArrayList<>())
                                        .build())
                                .build(),
                        true
                },
                {
                        // language=JSON
                        """
                        {
                            "email": "john.doe.gmail.com",
                            "password": "40580jkdjfJIJj"
                        }""",
                        null,
                        // language=JSON
                        """
                        {
                            "email":"must be a well-formed email address"
                        }""",
                        null,
                        false
                },
                {
                        // language=JSON
                        """
                        {
                            "email": "",
                            "password": ""
                        }""",
                        null,
                        // language=JSON
                        """
                        {
                            "email":"must not be blank"
                        }""",
                        null,
                        false
                }
        };
    }

    @ParameterizedTest
    @DisplayName("Test register new user")
    @MethodSource("registerDataProvider")
    @WithAnonymousUser
    void register(String requestJson,
                  RegisterRequest requestObj,
                  String responseJson,
                  AuthenticationResponse responseObj,
                  Boolean success) throws Exception {

        if (success) {
            when(authenticationService.register(requestObj, Role.ROLE_USER))
                    .thenReturn(responseObj);
        }

        // then
        mockMvc.perform(post(ROOT_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andDo(print())
                .andExpect(success ? status().isCreated() : status().isBadRequest())
                .andExpect(content().json(responseJson, true));
    }

    @Test
    @DisplayName("Test refresh token")
    @WithAnonymousUser
    void refreshToken() throws Exception {
        // given
        String refreshToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc3ODE3MDUsImV4cCI6MTcyNzc4MTczNX0.Vem92uCmIvcErFhri54NmQvxdk3qfElLcGJ9LZ_9TeCyO66v20_r8QeuCfUVMn_dTApmdHCyk-O9ARwgbcyrUw";

        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .accessToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc3ODE3MDUsImV4cCI6MTcyNzc4MTczNX0.Vem92uCmIvcErFhri54NmQvxdk3qfElLcGJ9LZ_9TeCyO66v20_r8QeuCfUVMn_dTApmdHCyk-O9ARwgbcyrUw")
                .refreshToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc3ODE3MDUsImV4cCI6MTcyNzc4MTc2NX0.HT5KK391gGf5oohjKOH_ky0Pp9ze43l4CLeygQCkwf7R4iLlv1oJ9PU4U6Ct3_SBxrSn1AW7T_kWqcEjF0Vrdg")
                .user(User.builder()
                        .id(UUID.fromString("b2e50470-fe61-4bf7-999f-b34d0908b9be"))
                        .firstName("John")
                        .lastName("Doe")
                        .email("john.doe@email.com")
                        .role(Role.ROLE_USER)
                        .build())
                .build();

        when(authenticationService.refreshToken(refreshToken)).thenReturn(expectedResponse);

        // then
        mockMvc.perform(post(ROOT_URL + "/refreshToken")
                        .contentType(MediaType.TEXT_PLAIN)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(refreshToken)
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(JsonUtils.toJson(expectedResponse), true));
    }

    static Object[][] registerDataProvider() {
        return new Object[][]{
                {
                        // language=JSON
                        """
                        {
                            "email": "john.doe@gmail.com",
                            "password": "40580jkdjfJIJj",
                            "firstName": "John",
                            "lastName": "Doe"
                        }""",
                        RegisterRequest.builder()
                                .email("john.doe@gmail.com")
                                .password("40580jkdjfJIJj")
                                .firstName("John")
                                .lastName("Doe")
                                .build(),
                        // language=JSON
                        """
                        {
                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJleHAiOjE2ODU4NzQ3MzksImlhdCI6MTY4NTc4ODMzOSwiYXV0aG9yaXRpZXMiOlt7ImF1dGhvcml0eSI6IkFETUlOIn1dfQ.G4axFV8xRkzcpLulzPXp-bRuRc3lQIs2vp_jkb6LxLw",
                            "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc2MDEwMzcsImV4cCI6MTcyNzYwMTE1N30.IyF9FgOzG_-6HxdJb7k-k0yY7oGoxPVtCG3MzKS0uKW-AmxTMrgN9GdaW5b0JnazJhAxsHCgV4ruxZ_GVEp-cQ",
                            "user": {
                                "id": "af11c153-2948-4922-bca7-3e407a40da02",
                                "firstName": "John",
                                "lastName": "Doe",
                                "email": "john.doe@gmail.com",
                                "role": "ROLE_USER"
                            }
                        }""",
                        AuthenticationResponse.builder()
                                .accessToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJleHAiOjE2ODU4NzQ3MzksImlhdCI6MTY4NTc4ODMzOSwiYXV0aG9yaXRpZXMiOlt7ImF1dGhvcml0eSI6IkFETUlOIn1dfQ.G4axFV8xRkzcpLulzPXp-bRuRc3lQIs2vp_jkb6LxLw")
                                .refreshToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYWJrYUBlbWFpbC5jb20iLCJpYXQiOjE3Mjc2MDEwMzcsImV4cCI6MTcyNzYwMTE1N30.IyF9FgOzG_-6HxdJb7k-k0yY7oGoxPVtCG3MzKS0uKW-AmxTMrgN9GdaW5b0JnazJhAxsHCgV4ruxZ_GVEp-cQ")
                                .user(User.builder()
                                        .id(UUID.fromString("af11c153-2948-4922-bca7-3e407a40da02"))
                                        .firstName("John")
                                        .lastName("Doe")
                                        .email("john.doe@gmail.com")
                                        .role(Role.ROLE_USER)
                                        .databases(new ArrayList<>())
                                        .build())
                                .build(),
                        true
                },
                {
                        // language=JSON
                        """
                        {
                            "email": "john.doe.gmail.com",
                            "password": "40580jkdjfJIJj",
                            "firstName": "too long first name too long first name too long first name too long first name",
                            "lastName": "too long last name too long last name too long last name too long last name"
                        }""",
                        null,
                        // language=JSON
                        """
                        {
                            "lastName": "size must be between 1 and 32",
                            "firstName":"size must be between 1 and 32",
                            "email":"must be a well-formed email address"
                        }""",
                        null,
                        false
                },
                {
                        // language=JSON
                        """
                        {
                            "email": "",
                            "password": "1",
                            "firstName": "",
                            "lastName": ""
                        }""",
                        null,
                        // language=JSON
                        """
                        {
                            "lastName": "size must be between 1 and 32",
                            "firstName": "size must be between 1 and 32",
                            "password": "size must be between 8 and 64",
                            "email":"must not be blank"
                        }""",
                        null,
                        false
                }
        };
    }
}