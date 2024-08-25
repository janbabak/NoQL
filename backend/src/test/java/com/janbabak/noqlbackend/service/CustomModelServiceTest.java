package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.CustomModelRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.customModel.ModelOption;
import com.janbabak.noqlbackend.model.customModel.UpdateCustomModelReqeust;
import com.janbabak.noqlbackend.model.entity.CustomModel;
import com.janbabak.noqlbackend.model.query.gpt.LlmModel;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomModelServiceTest {

    @InjectMocks
    private CustomModelService customModelService;

    @Mock
    private CustomModelRepository customModelRepository;

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
                .build();

        // when
        when(customModelRepository.findById(customModelId)).thenReturn(Optional.of(customModel));
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
                .build();

        CustomModel customModel2 = CustomModel.builder()
                .id(UUID.randomUUID())
                .name("CVUT model")
                .host("https://www.cvut.cz/llm")
                .port(8080)
                .build();

        List<CustomModel> customModels = List.of(customModel1, customModel2);

        // when
        when(customModelRepository.findAll()).thenReturn(customModels);
        List<CustomModel> actual = customModelService.findAll();

        // then
        assertEquals(2, actual.size());
        assertEquals(customModels, actual);
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
                .build();

        CustomModel customModel2 = CustomModel.builder()
                .id(UUID.randomUUID())
                .name("CVUT model")
                .host("https://www.cvut.cz/llm")
                .port(8080)
                .build();

        List<CustomModel> customModels = List.of(customModel1, customModel2);

        // when
        when(customModelRepository.findAll()).thenReturn(customModels);

        List<ModelOption> actual = customModelService.getAllModels();

        // then
        assertEquals(LlmModel.values().length + customModels.size(), actual.size());
    }

    @Test
    @DisplayName("Test create custom model")
    void testCreateCustomModel() {
        // given
        CustomModel customModel = CustomModel.builder()
                .name("Local model")
                .host("localhost")
                .port(8085)
                .build();

        // when
        when(customModelRepository.save(customModel)).thenReturn(customModel);
        CustomModel actual = customModelService.create(customModel);

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
                .build();

        // when
        when(customModelRepository.findById(customModelId)).thenReturn(Optional.of(customModel));
        when(customModelRepository.save(customModel)).thenReturn(updatedCustomModel);

        CustomModel actual = customModelService.update(customModelId, updateCustomModelReqeust);

        // then
        ArgumentCaptor<CustomModel> customModelCaptor = ArgumentCaptor.forClass(CustomModel.class);
        verify(customModelRepository).save(customModelCaptor.capture());
        assertEquals(updatedCustomModel, customModelCaptor.getValue());
        assertEquals(updatedCustomModel, actual);
    }

    @Test
    @DisplayName("Test delete custom model")
    void testDeleteCustomModel() throws EntityNotFoundException {
        // given
        UUID customModelId = UUID.randomUUID();

        // when
        customModelService.delete(customModelId);

        // then
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(customModelRepository).deleteById(idCaptor.capture());
        assertEquals(customModelId, idCaptor.getValue());
    }
}