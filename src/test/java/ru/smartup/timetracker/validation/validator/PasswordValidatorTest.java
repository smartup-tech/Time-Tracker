package ru.smartup.timetracker.validation.validator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PasswordValidatorTest {
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
    private final ConstraintValidatorContext.ConstraintViolationBuilder builder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    private final PasswordValidator validator = new PasswordValidator();

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"Admin12345", "AdMiN12345"})
    public void isValidShouldReturnTrue(String inputValue) {
        boolean result = validator.isValid(inputValue, context);

        verify(context, never()).disableDefaultConstraintViolation();
        verify(builder, never()).addConstraintViolation();
        assertTrue(result);
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"Pas1", "Password_with_size_greater_than_30", "admin123", "ADMIN123", "Admin", "      "})
    public void isValidShouldReturnFalse(String inputValue) {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        boolean result = validator.isValid(inputValue, context);

        verify(context).disableDefaultConstraintViolation();
        verify(builder).addConstraintViolation();
        assertFalse(result);
    }
}