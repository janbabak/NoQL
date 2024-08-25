package com.janbabak.noqlbackend.model.customModel;

import com.janbabak.noqlbackend.model.entity.CustomModel;

/**
 * Used as an option in frontend select element
 */
public record ModelOption(
        String label,
        String value) {

    public ModelOption(CustomModel customModel) {
        this(customModel.getName(), customModel.getId().toString());
    }
}
