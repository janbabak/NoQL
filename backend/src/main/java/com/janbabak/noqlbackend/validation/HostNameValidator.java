package com.janbabak.noqlbackend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validates hostname.
 */
public class HostNameValidator implements ConstraintValidator<ValidHostName, String> {

    private static final Pattern HOSTNAME_PATTERN = Pattern.compile(
            "^(localhost|(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)+([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9]))$");


    /**
     * Initializes the validator in preparation for isValid calls.
     *
     * @param value   object to validate
     * @param context context in which the constraint is evaluated
     * @return true if valid, false if not (null is valid, empty string is not)
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        } else if (value.isEmpty()) {
            return false;
        }
        return HOSTNAME_PATTERN.matcher(value).matches();
    }
}
