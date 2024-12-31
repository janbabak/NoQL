package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.CustomModelRepository;
import com.janbabak.noqlbackend.dao.repository.UserRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.customModel.CreateCustomModelRequest;
import com.janbabak.noqlbackend.model.customModel.ModelOption;
import com.janbabak.noqlbackend.model.customModel.UpdateCustomModelReqeust;
import com.janbabak.noqlbackend.model.entity.CustomModel;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.query.LlmModel;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomModelServiceTest {

    @InjectMocks
    private CustomModelService customModelService;

    @Mock
    private CustomModelRepository customModelRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    @SuppressWarnings("unused") // used int the customModelService
    private AuthenticationService authenticationService;

    private final User testUser = User.builder()
            .id(UUID.randomUUID())
            .build();

    @Test
    @DisplayName("Test find custom model by id")
    void testFindCustomModelById() throws EntityNotFoundException {
        // given
        UUID customModelId = UUID.randomUUID();

        CustomModel customModel = CustomModel.builder()
                .id(customModelId)
                .name("Local model")
                .host("localhost")
                .port(8085)
                .user(testUser)
                .build();

        when(customModelRepository.findById(customModelId)).thenReturn(Optional.of(customModel));

        // when
        CustomModel actual = customModelService.findById(customModelId);

        // then
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(customModelRepository).findById(idCaptor.capture());
        assertEquals(customModelId, idCaptor.getValue());
        assertEquals(customModel, actual);
    }

    @Test
    @DisplayName("Test find all custom models")
    void testFindAllCustomModels() {
        // given
        CustomModel customModel1 = CustomModel.builder()
                .id(UUID.randomUUID())
                .name("Local model")
                .host("localhost")
                .port(8085)
                .user(testUser)
                .build();

        CustomModel customModel2 = CustomModel.builder()
                .id(UUID.randomUUID())
                .name("CVUT model")
                .host("https://www.cvut.cz/llm")
                .port(8080)
                .user(testUser)
                .build();

        CustomModel customModel3 = CustomModel.builder()
                .id(UUID.randomUUID())
                .name("Different user's model")
                .host("https://www.cvut.cz/llm")
                .port(8080)
                .user(User.builder().id(UUID.randomUUID()).build())
                .build();

        List<CustomModel> allCustomModels = List.of(customModel1, customModel2, customModel3);
        List<CustomModel> testUsersCustomModels = List.of(customModel1, customModel2);

        when(customModelRepository.findAll()).thenReturn(allCustomModels);
        when(customModelRepository.findAllByUserId(testUser.getId())).thenReturn(testUsersCustomModels);

        // when
        List<CustomModel> actualAllModels = customModelService.findAll();
        List<CustomModel> actualTestUsersModels = customModelService.findAll(testUser.getId());

        // then
        assertEquals(3, actualAllModels.size());
        assertEquals(actualAllModels, allCustomModels);
        assertEquals(2, testUsersCustomModels.size());
        assertEquals(actualTestUsersModels, testUsersCustomModels);
    }

    @Test
    @DisplayName("Test get all models")
    void testGetAllModels() {
        // given
        CustomModel customModel1 = CustomModel.builder()
                .id(UUID.randomUUID())
                .name("Local model")
                .host("localhost")
                .port(8085)
                .user(testUser)
                .build();

        CustomModel customModel2 = CustomModel.builder()
                .id(UUID.randomUUID())
                .name("CVUT model")
                .host("https://www.cvut.cz/llm")
                .port(8080)
                .user(testUser)
                .build();

        List<CustomModel> customModels = List.of(customModel1, customModel2);

        when(customModelRepository.findAllByUserId(testUser.getId())).thenReturn(customModels);

        // when
        List<ModelOption> actual = customModelService.getAllModels(testUser.getId());

        // then
        assertEquals(LlmModel.values().length + customModels.size(), actual.size());
    }

    @Test
    @DisplayName("Test create custom model")
    void testCreateCustomModel() throws EntityNotFoundException {
        // given
        CreateCustomModelRequest request = CreateCustomModelRequest.builder()
                .name("Local model")
                .host("localhost")
                .port(8085)
                .userId(testUser.getId())
                .build();

        CustomModel customModel = CustomModel.builder()
                .name("Local model")
                .host("localhost")
                .port(8085)
                .user(testUser)
                .build();

        when(customModelRepository.save(any())).thenReturn(customModel);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // when
        CustomModel actual = customModelService.create(request);

        // then
        ArgumentCaptor<CustomModel> customModelCaptor = ArgumentCaptor.forClass(CustomModel.class);
        verify(customModelRepository).save(customModelCaptor.capture());
        assertEquals(customModel, customModelCaptor.getValue());
        assertEquals(customModel, actual);
    }

    @Test
    @DisplayName("Test update custom model")
    void testUpdateCustomModel() throws EntityNotFoundException {
        // given
        UUID customModelId = UUID.randomUUID();

        CustomModel customModel = CustomModel.builder()
                .id(customModelId)
                .name("Local model")
                .host("localhost")
                .port(8085)
                .user(testUser)
                .build();

        UpdateCustomModelReqeust updateCustomModelReqeust = UpdateCustomModelReqeust.builder()
                .name("CVUT model")
                .host("https://www.cvut.cz/llm")
                .port(8080)
                .build();

        CustomModel updatedCustomModel = CustomModel.builder()
                .id(customModelId)
                .name("CVUT model")
                .host("https://www.cvut.cz/llm")
                .port(8080)
                .user(testUser)
                .build();

        when(customModelRepository.findById(customModelId)).thenReturn(Optional.of(customModel));
        when(customModelRepository.save(customModel)).thenReturn(updatedCustomModel);

        // when
        CustomModel actual = customModelService.update(customModelId, updateCustomModelReqeust);

        // then
        ArgumentCaptor<CustomModel> customModelCaptor = ArgumentCaptor.forClass(CustomModel.class);
        verify(customModelRepository).save(customModelCaptor.capture());
        assertEquals(updatedCustomModel, customModelCaptor.getValue());
        assertEquals(updatedCustomModel, actual);
    }

    @Test
    @DisplayName("Test delete custom model")
    void testDeleteCustomModel() {
        // given
        UUID customModelId = UUID.randomUUID();
        CustomModel customModel = CustomModel.builder()
                .id(customModelId)
                .user(testUser)
                .build();

        when(customModelRepository.findById(customModelId)).thenReturn(Optional.of(customModel));

        // when
        customModelService.delete(customModelId);

        // then
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(customModelRepository).deleteById(idCaptor.capture());
        assertEquals(customModelId, idCaptor.getValue());
    }
}