package com.janbabak.noqlbackend.validation;

import jakarta.validation.GroupSequence;

@GroupSequence({FirstValidationGroup.class, SecondValidationGroup.class})
public interface ValidationSequence {
}
