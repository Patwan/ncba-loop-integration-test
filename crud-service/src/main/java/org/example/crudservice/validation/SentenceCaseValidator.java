package org.example.crudservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SentenceCaseValidator implements ConstraintValidator<SentenceCase, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            return true;
        }
        // First character must be an uppercase letter.
        // Character.isUpperCase() returns false for non-letters, so "123" or "_abc" also fail.
        return Character.isUpperCase(value.charAt(0));
    }
}