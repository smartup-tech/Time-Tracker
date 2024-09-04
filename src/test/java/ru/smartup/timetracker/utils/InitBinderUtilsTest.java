package ru.smartup.timetracker.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import ru.smartup.timetracker.dto.ErrorCode;
import ru.smartup.timetracker.exception.InvalidParameterException;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InitBinderUtilsTest {
    private final CustomDateEditor dateEditor = InitBinderUtils.getCustomLocalDateEditor();

    private static Stream<Arguments> provideValidData() {
        return Stream.of(
                Arguments.of("now()", LocalDate.now()),
                Arguments.of("2022-12-20", LocalDate.of(2022, 12, 20))
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidData")
    public void setAsTextShouldParseInputValue(String inputValue, LocalDate expectedDate) {
        dateEditor.setAsText(inputValue);
        LocalDate resultDate = (LocalDate) dateEditor.getValue();

        assertEquals(expectedDate, resultDate);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"20.12.2022", "20/12/2022", "20-12-2022", "* # )", "   "})
    public void setAsTextShouldThrowException(String inputValue) {
        InvalidParameterException ex = assertThrows(InvalidParameterException.class,
                () -> dateEditor.setAsText(inputValue));

        assertEquals(ErrorCode.NOT_VALID_DATE, ex.getErrorCode());
        assertEquals("Invalid parameter for date; value = " + inputValue, ex.getMessage());
    }
}