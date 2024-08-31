package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.customModel.ModelOption;
import com.janbabak.noqlbackend.model.entity.CustomModel;
import com.janbabak.noqlbackend.service.CustomModelService;
import com.janbabak.noqlbackend.service.JwtService;
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

import static com.janbabak.noqlbackend.service.utils.JsonUtils.toJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomModelController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(JwtService.class)
class CustomModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomModelService customModelService;

    private final String ROOT_URL = "/model";

    private final CustomModel localModel = CustomModel.builder()
            .id(UUID.randomUUID())
            .name("Local model")
            .host("localhost")
            .port(8085)
            .build();

    @Test
    @DisplayName("Get all custom models")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testGetAllCustomModels() throws Exception {
        // given
        CustomModel gptProxy = CustomModel.builder()
                .id(UUID.randomUUID())
                .name("Gpt proxy")
                .host("localhost")
                .port(8086)
                .build();

        List<CustomModel> customModels = List.of(localModel, gptProxy);

        // when
        when(customModelService.findAll()).thenReturn(customModels);

        // then
        mockMvc.perform(get(ROOT_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(customModels)));
    }

    @Test
    @DisplayName("Get all models")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
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

        // when
        when(customModelService.getAllModels()).thenReturn(allModels);

        // then
        mockMvc.perform(get(ROOT_URL + "/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(allModels)));
    }

    @Test
    @DisplayName("Get custom model by id")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testGetCustomModelById() throws Exception {
        // when
        when(customModelService.findById(localModel.getId())).thenReturn(localModel);

        // then
        mockMvc.perform(get(ROOT_URL + "/{localModelId}", localModel.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(localModel)));
    }

    @Test
    @DisplayName("Get custom model by id not found")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testGetCustomModelByIdNotFound() throws Exception {
        // given
        UUID customModelId = UUID.randomUUID();

        // when
        when(customModelService.findById(customModelId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{localModelId}", customModelId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource("createCustomModelDataProvider")
    @DisplayName("Create custom model")
    void testCreateCustomModel(String request, CustomModel createdModel, String response, Boolean success)
            throws Exception {

        // when
        if (success) {
            when(customModelService.create(any())).thenReturn(createdModel);
        }

        // then
        mockMvc.perform(post(ROOT_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(print())
                .andExpect(success ? status().isCreated() : status().isBadRequest())
                .andExpect(content().json(response, true));
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
                            "port": 8085
                        }""",
                        CustomModel.builder()
                                .id(UUID.fromString("6678fc72-1a55-4146-b74b-b3f5aac677df"))
                                .name("Local model")
                                .host("localhost")
                                .port(8085)
                                .build(),
                        // language=JSON
                        """
                        {
                            "id": "6678fc72-1a55-4146-b74b-b3f5aac677df",
                            "name": "Local model",
                            "host": "localhost",
                            "port": 8085
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
                             "name": "must not be blank"
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
                            "host": "https://www.cvut.cz/llm",
                            "port": 8888
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
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testUpdateCustomModel(String request, CustomModel updatedModel, String response, Boolean success)
            throws Exception {

        // given
        UUID customModelId = UUID.fromString("6678fc72-1a55-4146-b74b-b3f5aac677df");

        // when
        if (success) {
            when(customModelService.update(eq(customModelId), any())).thenReturn(updatedModel);
        }

        // then
        mockMvc.perform(put(ROOT_URL + "/{localModelId}", customModelId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(print())
                .andExpect(success ? status().isOk() : status().isBadRequest())
                .andExpect(content().json(response, true));
    }

    /**
     * @return request, updated model, response, and success
     */
    Object[][] updatedCustomModelDataProvider() {
        return new Object[][] {
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
                                .build(),
                        // language=JSON
                        """
                        {
                            "id": "6678fc72-1a55-4146-b74b-b3f5aac677df",
                            "name": "Local model updated",
                            "host": "localhost",
                            "port": 8086
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
    @DisplayName("Delete custom model by id")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testDeleteCustomModelById() throws Exception {
        // given
        UUID customModelId = UUID.randomUUID();

        // then
        mockMvc.perform(delete(ROOT_URL + "/{localModelId}", customModelId))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

}