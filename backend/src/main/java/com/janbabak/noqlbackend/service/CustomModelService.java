package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.CustomModelRepository;
import com.janbabak.noqlbackend.dao.repository.UserRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.customModel.CreateCustomModelRequest;
import com.janbabak.noqlbackend.model.customModel.ModelOption;
import com.janbabak.noqlbackend.model.customModel.UpdateCustomModelReqeust;
import com.janbabak.noqlbackend.model.entity.CustomModel;
import com.janbabak.noqlbackend.model.entity.User;
import com.janbabak.noqlbackend.model.query.gpt.LlmModel;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.CUSTOM_MODEL;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomModelService {

    private final CustomModelRepository customModelRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    /**
     * Find custom model by id.
     *
     * @param customModelId custom model identifier
     * @return custom model
     * @throws EntityNotFoundException                                   model of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of models.
     */
    public CustomModel findById(UUID customModelId) throws EntityNotFoundException {
        log.info("Get custom model by id={}.", customModelId);

        CustomModel model = customModelRepository.findById(customModelId)
                .orElseThrow(() -> new EntityNotFoundException(CUSTOM_MODEL, customModelId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(model.getUserId());

        return model;
    }

    /**
     * Find all custom models (from all users).
     *
     * @return list of custom models
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin.
     */
    public List<CustomModel> findAll() {
        return findAll(null);
    }

    /**
     * Find all custom models with filter.
     *
     * @param userId user identifier - filter by user id. When null - return all custom models.
     *               User can see only his custom models. Admin can see all.
     * @return list of custom models
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of models.
     */
    public List<CustomModel> findAll(UUID userId) {
        log.info("Get all custom models. Filter by userId={}.", userId);

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(userId);

        return userId == null
                ? customModelRepository.findAll()
                : customModelRepository.findAllByUserId(userId);
    }

    /**
     * Get all custom model (filtered by userId) ids plus not custom models like gpt, ...
     *
     * @return model ids
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of models.
     */
    public List<ModelOption> getAllModels(UUID userId) {
        // add default models
        List<ModelOption> models = new java.util.ArrayList<>(Arrays.stream(LlmModel.values())
                .map(model -> new ModelOption(model.getLabel(), model.getModel()))
                .toList());

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(userId);

        // add custom models
        models.addAll(userId == null
                ? customModelRepository.findAll().stream().map(ModelOption::new).toList()
                : customModelRepository.findAllByUserId(userId).stream().map(ModelOption::new).toList());

        return models;
    }

    /**
     * Create new custom model object - persist it.
     *
     * @param request custom model data
     * @return saved object with id
     * @throws EntityNotFoundException                                   if user with specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the model.
     */
    public CustomModel create(CreateCustomModelRequest request) throws EntityNotFoundException {
        log.info("Save custom model.");

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(request.userId());

        User user = userRepository.findById(request.userId()).orElseThrow(
                () -> new EntityNotFoundException(EntityNotFoundException.Entity.USER, request.userId()));

        CustomModel customModel = CustomModel.builder()
                .name(request.name())
                .host(request.host())
                .port(request.port())
                .user(user)
                .build();

        return customModelRepository.save(customModel);
    }

    /**
     * Update custom model object - persist it.
     *
     * @param data object to be updated
     * @return updated object
     * @throws EntityNotFoundException                                   if custom model with specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the model.
     */
    public CustomModel update(UUID customModelId, UpdateCustomModelReqeust data) throws EntityNotFoundException {
        log.info("Update custom model.");

        CustomModel customModel = customModelRepository.findById(customModelId)
                .orElseThrow(() -> new EntityNotFoundException(CUSTOM_MODEL, customModelId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(customModel.getUserId());

        if (data.getName() != null) customModel.setName(data.getName());
        if (data.getHost() != null) customModel.setHost(data.getHost());
        if (data.getPort() != null) customModel.setPort(data.getPort());

        return customModelRepository.save(customModel);
    }

    /**
     * Delete custom model by id.
     *
     * @param customModelId custom model identifier
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the model.
     */
    public void delete(UUID customModelId) {
        log.info("Delete custom model by id={}.", customModelId);

        Optional<CustomModel> customModel = customModelRepository.findById(customModelId);

        if (customModel.isPresent()) {
            authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(customModel.get().getUserId());
            customModelRepository.deleteById(customModelId);
        }
    }
}