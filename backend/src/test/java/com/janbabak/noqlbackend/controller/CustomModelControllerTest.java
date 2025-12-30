package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.custommodel.ModelOption;
import com.janbabak.noqlbackend.model.entity.CustomModel;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.service.CustomModelService;
import com.janbabak.noqlbackend.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.janbabak.noqlbackend.service.utils.JsonUtils.toJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(JwtService.class)
class CustomModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomModelService customModelServiceMock;

    private final String ROOT_URL = "/model";

    private final User testUser = User.builder()
            .id(UUID.fromString("af11c153-2948-4922-bca7-3e407a40da02"))
            .build();

    private final CustomModel localModel = CustomModel.builder()
            .id(UUID.randomUUID())
            .name("Local model")
            .host("localhost")
            .port(8085)
            .user(testUser)
            .build();

    @Test
    @DisplayName("Get all custom models")
    @WithMockUser(roles = "USER")
    void testGetAllCustomModels() throws Exception {
        // given
        CustomModel gptProxy = CustomModel.builder()
                .id(UUID.randomUUID())
                .name("Gpt proxy")
                .host("localhost")
                .port(8086)
                .user(testUser)
                .build();

        List<CustomModel> customModels = List.of(localModel, gptProxy);

        when(customModelServiceMock.findAll(testUser.getId())).thenReturn(customModels);

        // then
        mockMvc.perform(get(ROOT_URL).param("userId", testUser.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(customModels)));
    }

    @Test
    @DisplayName("Get all custom models with anonymous user")
    @WithAnonymousUser
    void testGetAllCustomModelsWithAnonymousUser() throws Exception {
        mockMvc.perform(get(ROOT_URL))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get all models")
    @WithMockUser(roles = "USER")
    void testGetAllModels() throws Exception {
        // given
        List<ModelOption> allModels = List.of(
                ModelOption.builder()
                        .label("GPT 4")
                        .value("gpt-4")
                        .build(),
                ModelOption.builder()
                        .label("My custom model")
                        .value("6678fc72-1a55-4146-b74b-b3f5aac677df")
                        .build());

        when(customModelServiceMock.getAllModels(testUser.getId())).thenReturn(allModels);

        // then
        mockMvc.perform(get(ROOT_URL + "/all").param("userId", testUser.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(allModels)));
    }

    @Test
    @DisplayName("Get all models with anonymous user")
    @WithAnonymousUser
    void testGetAllModelsWithAnonymousUser() throws Exception {
        mockMvc.perform(get(ROOT_URL + "/all"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get custom model by id")
    @WithMockUser(roles = "USER")
    void testGetCustomModelById() throws Exception {
        when(customModelServiceMock.findById(localModel.getId())).thenReturn(localModel);

        // then
        mockMvc.perform(get(ROOT_URL + "/{localModelId}", localModel.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(localModel), true));
    }

    @Test
    @DisplayName("Get custom model by id not found")
    @WithMockUser(roles = "USER")
    void testGetCustomModelByIdNotFound() throws Exception {
        // given
        UUID customModelId = UUID.randomUUID();

        when(customModelServiceMock.findById(customModelId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{localModelId}", customModelId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get custom model by id with anonymous user")
    @WithAnonymousUser
    void testGetCustomModelByIdWithAnonymousUser() throws Exception {
        mockMvc.perform(get(ROOT_URL + "/{localModelId}", localModel.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("createCustomModelDataProvider")
    @DisplayName("Create custom model")
    @WithMockUser(roles = "USER")
    void testCreateCustomModel(String request, CustomModel createdModel, String response, Boolean success)
            throws Exception {

        if (success) {
            when(customModelServiceMock.create(any())).thenReturn(createdModel);
        }

        // then
        mockMvc.perform(post(ROOT_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
                        .with(csrf()))
                .andDo(print())
                .andExpect(success ? status().isCreated() : status().isBadRequest())
                .andExpect(content().json(response, true));
    }

    @Test
    @DisplayName("Create custom model with anonymous user")
    @WithAnonymousUser
    void testCreateCustomModelWithAnonymousUser() throws Exception {
        mockMvc.perform(post(ROOT_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    /**
     * @return request, created model, response, and success
     */
    Object[][] createCustomModelDataProvider() {
        return new Object[][]{
                {
                        // language=JSON
                        """
                        {
                            "name": "Local model",
                            "host": "localhost",
                            "port": 8085,
                            "userId": "af11c153-2948-4922-bca7-3e407a40da02"
                        }""",
                        CustomModel.builder()
                                .id(UUID.fromString("6678fc72-1a55-4146-b74b-b3f5aac677df"))
                                .name("Local model")
                                .host("localhost")
                                .port(8085)
                                .user(testUser)
                                .build(),
                        // language=JSON
                        """
                        {
                            "id": "6678fc72-1a55-4146-b74b-b3f5aac677df",
                            "name": "Local model",
                            "host": "localhost",
                            "port": 8085,
                            "userId": "af11c153-2948-4922-bca7-3e407a40da02"
                        }""",
                        true
                },
                {
                        // language=JSON
                        """
                        {
                            "name": "",
                            "host": ""
                        }""",
                        null,
                        // language=JSON
                        """
                        {
                             "port": "must not be null",
                             "host": "must not be blank",
                             "name": "must not be blank",
                             "userId": "must not be null"
                        }""",
                        false
                },
                {
                        // language=JSON
                        """
                        {
                            "name": "test",
                            "host": "https://www.cvut.cz/llm",
                            "port": -304,
                            "userId": "af11c153-2948-4922-bca7-3e407a40da02"
                        }""",
                        null,
                        // language=JSON
                        """
                        {
                             "port": "must be greater than or equal to 1"
                        }""",
                        false
                },
                {
                        // language=JSON
                        """
                        {
                            "name": "test name is longer than maximum length",
                            "host": "https://www.cvut.cz/llm",
                            "port": 8888,
                            "userId": "af11c153-2948-4922-bca7-3e407a40da02"
                        }""",
                        null,
                        // language=JSON
                        """
                        {
                            "name": "length must be between 1 and 32"
                        }""",
                        false
                },
        };
    }

    @ParameterizedTest
    @MethodSource("updatedCustomModelDataProvider")
    @DisplayName("Update custom model")
    @WithMockUser(roles = "USER")
    void testUpdateCustomModel(String request, CustomModel updatedModel, String response, Boolean success)
            throws Exception {

        // given
        UUID customModelId = UUID.fromString("6678fc72-1a55-4146-b74b-b3f5aac677df");

        if (success) {
            when(customModelServiceMock.update(eq(customModelId), any())).thenReturn(updatedModel);
        }

        // then
        mockMvc.perform(put(ROOT_URL + "/{localModelId}", customModelId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
                        .with(csrf()))
                .andDo(print())
                .andExpect(success ? status().isOk() : status().isBadRequest())
                .andExpect(content().json(response, true));
    }

    /**
     * @return request, updated model, response, and success
     */
    Object[][] updatedCustomModelDataProvider() {
        return new Object[][]{
                {
                        // language=JSON
                        """
                        {
                            "name": "Local model updated",
                            "host": "localhost",
                            "port": 8086
                        }""",
                        CustomModel.builder()
                                .id(UUID.fromString("6678fc72-1a55-4146-b74b-b3f5aac677df"))
                                .name("Local model updated")
                                .host("localhost")
                                .port(8086)
                                .user(testUser)
                                .build(),
                        // language=JSON
                        """
                        {
                            "id": "6678fc72-1a55-4146-b74b-b3f5aac677df",
                            "name": "Local model updated",
                            "host": "localhost",
                            "port": 8086,
                            "userId": "af11c153-2948-4922-bca7-3e407a40da02"
                        }""",
                        true
                },
                {
                        // language=JSON
                        """
                        {
                            "name": "",
                            "host": ""
                        }""",
                        null,
                        // language=JSON
                        """
                        {
                             "host": "length must be between 1 and 253",
                             "name": "length must be between 1 and 32"
                        }""",
                        false
                },
                {
                        // language=JSON
                        """
                        {
                            "name": "test",
                            "host": "https://www.cvut.cz/llm",
                            "port": -304
                        }""",
                        null,
                        // language=JSON
                        """
                        {
                             "port": "must be greater than or equal to 1"
                        }""",
                        false
                },
                {
                        // language=JSON
                        """
                        {
                            "name": "test name is longer than maximum length",
                            "host": "https://www.cvutttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt._ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt.ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt.cz/llm",
                            "port": 8888
                        }""",
                        null,
                        // language=JSON
                        """
                        {
                            "name": "length must be between 1 and 32",
                            "host": "length must be between 1 and 253"
                        }""",
                        false
                },
        };
    }

    @Test
    @DisplayName("Update custom model with anonymous user")
    @WithAnonymousUser
    void testUpdateCustomModelWithAnonymousUser() throws Exception {
        mockMvc.perform(put(ROOT_URL + "/{localModelId}", localModel.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Delete custom model by id")
    @WithMockUser(roles = "USER")
    void testDeleteCustomModelById() throws Exception {
        // given
        UUID customModelId = UUID.randomUUID();

        // then
        mockMvc.perform(delete(ROOT_URL + "/{localModelId}", customModelId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete custom model with anonymous user")
    @WithAnonymousUser
    void testDeleteCustomModelWithAnonymousUser() throws Exception {
        mockMvc.perform(delete(ROOT_URL + "/{localModelId}", localModel.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

}