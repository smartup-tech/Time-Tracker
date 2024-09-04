package ru.smartup.timetracker.validation;

import ru.smartup.timetracker.validation.validator.OptionalFieldValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OptionalFieldValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface OptionalField {
    String message() default "invalid field";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int maxSize();
}
