package ru.smartup.timetracker.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonStringUtilsTest {
    private static Stream<Arguments> provideDataForEscaping() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY),
                Arguments.of("   ", "   "),
                Arguments.of("not blank", "not blank"),
                Arguments.of(" not%blank ", " not\\%blank "),
                Arguments.of(" not_blank ", " not\\_blank "),
                Arguments.of(" not%_blank ", " not\\%\\_blank "),
                Arguments.of(" not%%__blank ", " not\\%\\%\\_\\_blank "),
                Arguments.of("not\\blank", "not\\blank")
        );
    }

    private static Stream<Arguments> provideDataForHashing() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
                Arguments.of(StringUtils.SPACE, "36a9e7f1c95b82ffb99743e0c5c4ce95d83c9a430aac59f84ef3cbfab6145068"),
                Arguments.of("abc", "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForEscaping")
    public void testEscapePercentAndUnderscore(String inputValue, String expectedValue) {
        String result = CommonStringUtils.escapePercentAndUnderscore(inputValue);

        assertEquals(expectedValue, result);
    }

    @ParameterizedTest
    @MethodSource("provideDataForHashing")
    public void testHashSHA256(String inputValue, String expectedValue) {
        String result = CommonStringUtils.hashSHA256(inputValue);

        assertEquals(expectedValue, result);
    }
}