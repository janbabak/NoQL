package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.CustomModelRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.customModel.UpdateCustomModelReqeust;
import com.janbabak.noqlbackend.model.entity.CustomModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.CUSTOM_MODEL;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomModelService {

    private final CustomModelRepository customModelRepository;

    /**
     * Find custom model by id.
     *
     * @param customModelId custom model identifier
     * @return custom model
     * @throws EntityNotFoundException model of specified id not found.
     */
    public CustomModel findById(UUID customModelId) throws EntityNotFoundException {
        log.info("Get custom model by id={}.", customModelId);

        return customModelRepository.findById(customModelId)
                .orElseThrow(() -> new EntityNotFoundException(CUSTOM_MODEL, customModelId));
    }

    /**
     * Find all custom models.
     *
     * @return list of custom models
     */
    public List<CustomModel> findAll() {
        log.info("Get all custom models.");

        return customModelRepository.findAll();
    }

    /**
     * Create new custom model object - persist it.
     *
     * @param customModel object to be saved
     * @return saved object with id
     */
    public CustomModel create(CustomModel customModel) {
        log.info("Save custom model.");

        return customModelRepository.save(customModel);
    }

    /**
     * Update custom model object - persist it.
     *
     * @param data object to be updated
     * @return updated object
     * @throws EntityNotFoundException if custom model with specified id not found.
     */
    public CustomModel update(UUID customModelId, UpdateCustomModelReqeust data) throws EntityNotFoundException {
        log.info("Update custom model.");

        CustomModel customModel = customModelRepository.findById(customModelId)
                .orElseThrow(() -> new EntityNotFoundException(CUSTOM_MODEL, customModelId));

        if (data.getName() != null) customModel.setName(data.getName());
        if (data.getHost() != null) customModel.setHost(data.getHost());
        if (data.getPort() != null) customModel.setPort(data.getPort());

        return customModelRepository.save(customModel);
    }

    /**
     * Delete custom model by id.
     *
     * @param customModelId custom model identifier
     */
    public void delete(UUID customModelId) throws EntityNotFoundException {
        log.info("Delete custom model by id={}.", customModelId);

        customModelRepository.deleteById(customModelId);
    }
}