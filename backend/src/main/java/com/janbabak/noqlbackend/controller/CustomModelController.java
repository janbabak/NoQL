package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.customModel.UpdateCustomModelReqeust;
import com.janbabak.noqlbackend.model.entity.CustomModel;
import com.janbabak.noqlbackend.service.CustomModelService;
import com.janbabak.noqlbackend.validation.ValidationSequence;
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
     * @return list of custom models
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CustomModel> getAll() {
        return customModelService.findAll();
    }

    /**
     * Get custom model by id.
     *
     * @param customModelId custom model identifier
     * @return custom model
     * @throws EntityNotFoundException model of specified id not found.
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
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomModel create(@Validated(ValidationSequence.class) @RequestBody CustomModel request) {
        return customModelService.create(request);
    }

    /**
     * Update custom model object - persist it.
     *
     * @param customModelId custom model identifier
     * @param request object to be updated
     * @return updated object
     * @throws EntityNotFoundException model of specified id not found.
     */
    @PutMapping("/{customModelId}")
    @ResponseStatus(HttpStatus.OK)
    public CustomModel update(@PathVariable UUID customModelId,
                              @Validated(ValidationSequence.class) @RequestBody UpdateCustomModelReqeust request)
            throws EntityNotFoundException {

        return customModelService.update(customModelId, request);
    }

    /**
     * Delete custom model by id.
     *
     * @param customModelId custom model identifier
     * @throws EntityNotFoundException model of specified id not found.
     */
    @DeleteMapping("/{customModelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID customModelId) throws EntityNotFoundException {
        customModelService.delete(customModelId);
    }
}
