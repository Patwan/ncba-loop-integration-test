package org.example.crudservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SentenceCaseValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SentenceCase {

    String message() default "Please convert to sentence case before consuming this endpoint";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}