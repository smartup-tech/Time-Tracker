package ru.smartup.timetracker.validation.validator;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.smartup.timetracker.validation.OptionalField;

import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OptionalFieldValidatorTest {
    private static final int MAX_SIZE = 15;
    private static final OptionalFieldValidator validator = new OptionalFieldValidator();

    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
    private final ConstraintValidatorContext.ConstraintViolationBuilder builder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

    @BeforeAll
    public static void setUp() {
        validator.initialize(createAnnotation());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"valid value"})
    public void isValidShouldReturnTrue(String inputValue) {
        boolean result = validator.isValid(inputValue, context);

        verify(context).disableDefaultConstraintViolation();
        verify(builder, never()).addConstraintViolation();
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {StringUtils.EMPTY, "   ", "value with size greater than maxSize"})
    public void isValidShouldReturnFalse(String inputValue) {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        boolean result = validator.isValid(inputValue, context);

        verify(context).disableDefaultConstraintViolation();
        verify(builder).addConstraintViolation();
        assertFalse(result);
    }

    private static OptionalField createAnnotation() {
        return new OptionalField() {
            @Override
            public String message() {
                return "invalid field";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public int maxSize() {
                return MAX_SIZE;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }
        };
    }
}