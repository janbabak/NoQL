package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.customModel.CreateCustomModelRequest;
import com.janbabak.noqlbackend.model.customModel.ModelOption;
import com.janbabak.noqlbackend.model.customModel.UpdateCustomModelReqeust;
import com.janbabak.noqlbackend.model.entity.CustomModel;
import com.janbabak.noqlbackend.service.CustomModelService;
import com.janbabak.noqlbackend.validation.ValidationSequence;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/model", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CustomModelController {

    private final CustomModelService customModelService;

    /**
     * Get all custom models.
     *
     * @param userId optional user id to get only models of this user (admin can get all models, user only his models)
     * @return list of custom models
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the model.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CustomModel> getAll(@RequestParam(required = false) UUID userId) {
        return customModelService.findAll(userId);
    }

    /**
     * Get all custom model ids plus not custom models like gpt, ...
     *
     * @param userId optional user id to get only models of this user (admin can get all models, user only his models)
     * @return ids of all models
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the model.
     */
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<ModelOption> getAllModelIds(@RequestParam(required = false) UUID userId) {
        return customModelService.getAllModels(userId);
    }

    /**
     * Get custom model by id.
     *
     * @param customModelId custom model identifier
     * @return custom model
     * @throws EntityNotFoundException                                   model of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the model.
     */
    @GetMapping("/{customModelId}")
    @ResponseStatus(HttpStatus.OK)
    public CustomModel getById(@PathVariable UUID customModelId) throws EntityNotFoundException {
        return customModelService.findById(customModelId);
    }

    /**
     * Create new custom model object - persist it.
     *
     * @param request object to be saved
     * @return saved object with id
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the model.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomModel create(@Validated(ValidationSequence.class) @RequestBody CreateCustomModelRequest request)
            throws EntityNotFoundException {
        return customModelService.create(request);
    }

    /**
     * Update custom model object - persist it.
     *
     * @param customModelId custom model identifier
     * @param request       object to be updated
     * @return updated object
     * @throws EntityNotFoundException                                   model of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the model.
     */
    @PutMapping("/{customModelId}")
    @ResponseStatus(HttpStatus.OK)
    public CustomModel update(@PathVariable UUID customModelId, @Valid @RequestBody UpdateCustomModelReqeust request)
            throws EntityNotFoundException {

        return customModelService.update(customModelId, request);
    }

    /**
     * Delete custom model by id.
     *
     * @param customModelId custom model identifier
     * @throws EntityNotFoundException                                   model of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if user is not admin or owner of the model.
     */
    @DeleteMapping("/{customModelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID customModelId) throws EntityNotFoundException {
        customModelService.delete(customModelId);
    }
}
