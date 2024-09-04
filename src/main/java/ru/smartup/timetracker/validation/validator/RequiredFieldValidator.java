package ru.smartup.timetracker.validation.validator;

import org.apache.commons.lang3.StringUtils;
import ru.smartup.timetracker.validation.RequiredField;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RequiredFieldValidator implements ConstraintValidator<RequiredField, String> {
    private int maxSize;

    @Override
    public void initialize(RequiredField constraintAnnotation) {
        this.maxSize = constraintAnnotation.maxSize();
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        if (value == null || StringUtils.EMPTY.equals(value) || StringUtils.isWhitespace(value)) {
            customMessage(context, "the field is blank");
            return false;
        }
        if (value.length() > maxSize) {
            customMessage(context, "the field size is greater than " + maxSize);
            return false;
        }
        return true;
    }

    private void customMessage(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
