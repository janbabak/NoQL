package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.model.user.AuthenticationRequest;
import com.janbabak.noqlbackend.model.user.AuthenticationResponse;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.user.RegisterRequest;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import com.janbabak.noqlbackend.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import(JwtService.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    private final String ROOT_URL = "/auth";

    @Test
    @DisplayName("Authenticate user")
    @WithMockUser
        // TODO: for some reason doesn't work without it, though it should work with @WithAnonymousUser
    void authenticate() throws Exception {
        // given
        AuthenticationRequest request = new AuthenticationRequest(
                "john.doe@gmail.com",
                "40580jkdjfJIJj");

        // language=JSON
        String requestContent = """
                {
                    "email": "john.doe@gmail.com",
                    "password": "40580jkdjfJIJj"
                }""";
        AuthenticationResponse response = new AuthenticationResponse(
                "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJleHAiOjE2ODU4NzQ3MzksImlhdCI6MTY4NTc4ODMzOSwiYXV0aG9yaXRpZXMiOlt7ImF1dGhvcml0eSI6IkFETUlOIn1dfQ.G4axFV8xRkzcpLulzPXp-bRuRc3lQIs2vp_jkb6LxLw",
                new User(UUID.fromString("af11c153-2948-4922-bca7-3e407a40da02"),
                        "John",
                        "Doe",
                        "john.doe@gmail.com",
                        "40580jkdjfJIJj",
                        Role.USER,
                        new ArrayList<>()));
        // language=JSON
        String responseContent = """

                {
                    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJleHAiOjE2ODU4NzQ3MzksImlhdCI6MTY4NTc4ODMzOSwiYXV0aG9yaXRpZXMiOlt7ImF1dGhvcml0eSI6IkFETUlOIn1dfQ.G4axFV8xRkzcpLulzPXp-bRuRc3lQIs2vp_jkb6LxLw",
                    "user": {
                        "id": "af11c153-2948-4922-bca7-3e407a40da02",
                        "firstName": "John",
                        "lastName": "Doe",
                        "email": "john.doe@gmail.com",
                        "role": "USER"
                    }
                }""";

        when(authenticationService.authenticate(request)).thenReturn(response);

        // then
        mockMvc.perform(post(ROOT_URL + "/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestContent)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(responseContent, true));
    }

    @Test
    @DisplayName("Authenticate users - bad request")
    @WithMockUser
        // TODO: for some reason doesn't work without it, though it should work with @WithAnonymousUser
    void authenticateBadRequest() throws Exception {
        // given
        // language=JSON
        String requestContent = """
                {
                    "email": "john.doe.gmail.com",
                    "password": "40580jkdjfJIJj"
                }""";

        // language=JSON
        String responseContent = """
                {
                    "email":"must be a well-formed email address"
                }""";

        // then
        mockMvc.perform(post(ROOT_URL + "/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestContent)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseContent, true));
    }

    @Test
    @DisplayName("Test register new user")
    @WithMockUser
        // TODO: for some reason doesn't work without it, though it should work with @WithAnonymousUser
    void register() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "john.doe@gmail.com",
                "40580jkdjfJIJj");

        // language=JSON
        String requestContent = """
                {
                    "email": "john.doe@gmail.com",
                    "password": "40580jkdjfJIJj",
                    "firstName": "John",
                    "lastName": "Doe"
                }""";
        AuthenticationResponse response = new AuthenticationResponse(
                "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJleHAiOjE2ODU4NzQ3MzksImlhdCI6MTY4NTc4ODMzOSwiYXV0aG9yaXRpZXMiOlt7ImF1dGhvcml0eSI6IkFETUlOIn1dfQ.G4axFV8xRkzcpLulzPXp-bRuRc3lQIs2vp_jkb6LxLw",
                new User(
                        UUID.fromString("af11c153-2948-4922-bca7-3e407a40da02"),
                        "John",
                        "Doe",
                        "john.doe@gmail.com",
                        "40580jkdjfJIJj",
                        Role.USER,
                        new ArrayList<>()));

        // language=JSON
        String responseContent = """

                {
                    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJleHAiOjE2ODU4NzQ3MzksImlhdCI6MTY4NTc4ODMzOSwiYXV0aG9yaXRpZXMiOlt7ImF1dGhvcml0eSI6IkFETUlOIn1dfQ.G4axFV8xRkzcpLulzPXp-bRuRc3lQIs2vp_jkb6LxLw",
                    "user": {
                        "id": "af11c153-2948-4922-bca7-3e407a40da02",
                        "firstName": "John",
                        "lastName": "Doe",
                        "email": "john.doe@gmail.com",
                        "role": "USER"
                    }
                }""";
        given(authenticationService.register(request, Role.USER))
                .willReturn(response);

        // then
        mockMvc.perform(post(ROOT_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestContent)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(responseContent, true));
    }

    @ParameterizedTest
    @DisplayName("Test register new user")
    @MethodSource("registerDataProvider")
    @WithMockUser
        // TODO: for some reason doesn't work without it, though it should work with @WithAnonymousUser
    void registerP(String requestJson,
                   RegisterRequest requestObj,
                   String responseJson,
                   AuthenticationResponse responseObj,
                   Boolean success) throws Exception {

        if (success) {
            when(authenticationService.register(requestObj, Role.USER))
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
                            "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJleHAiOjE2ODU4NzQ3MzksImlhdCI6MTY4NTc4ODMzOSwiYXV0aG9yaXRpZXMiOlt7ImF1dGhvcml0eSI6IkFETUlOIn1dfQ.G4axFV8xRkzcpLulzPXp-bRuRc3lQIs2vp_jkb6LxLw",
                            "user": {
                                "id": "af11c153-2948-4922-bca7-3e407a40da02",
                                "firstName": "John",
                                "lastName": "Doe",
                                "email": "john.doe@gmail.com",
                                "role": "USER"
                            }
                        }""",
                        AuthenticationResponse.builder()
                                .token("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJleHAiOjE2ODU4NzQ3MzksImlhdCI6MTY4NTc4ODMzOSwiYXV0aG9yaXRpZXMiOlt7ImF1dGhvcml0eSI6IkFETUlOIn1dfQ.G4axFV8xRkzcpLulzPXp-bRuRc3lQIs2vp_jkb6LxLw")
                                .user(User.builder()
                                        .id(UUID.fromString("af11c153-2948-4922-bca7-3e407a40da02"))
                                        .firstName("John")
                                        .lastName("Doe")
                                        .email("john.doe@gmail.com")
                                        .role(Role.USER)
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