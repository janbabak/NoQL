package com.janbabak.noqlbackend.validation;

import jakarta.validation.GroupSequence;

/**
 * Defines in which order the validation groups should be validated.
 */
@GroupSequence({FirstValidationGroup.class, SecondValidationGroup.class})
public interface ValidationSequence {
}
