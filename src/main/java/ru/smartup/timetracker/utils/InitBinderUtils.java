package ru.smartup.timetracker.utils;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import lombok.experimental.UtilityClass;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import ru.smartup.timetracker.dto.ErrorCode;
import ru.smartup.timetracker.exception.InvalidParameterException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class InitBinderUtils {
    private static final String PARAM_VALUE_DATE_NOW = "now()";
    private static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
    private static final String ERROR_MESSAGE = "Invalid parameter for date; value = ";

    /**
     * RequestParam(defaultValue = "now()") LocalDate dateOfWeek вернет текущую дату в случае, если не передано значение
     * или передано "now()"
     *
     * @return CustomDateEditor
     */
    public static CustomDateEditor getCustomLocalDateEditor() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_YYYY_MM_DD);
        return new CustomDateEditor(new StdDateFormat(), false) {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (PARAM_VALUE_DATE_NOW.equals(text)) {
                    setValue(LocalDate.now());
                    return;
                }
                try {
                    setValue(LocalDate.parse(text, dateTimeFormatter));
                } catch (Exception e) {
                    throw new InvalidParameterException(ErrorCode.NOT_VALID_DATE, ERROR_MESSAGE + text, e);
                }
            }
        };
    }
}
