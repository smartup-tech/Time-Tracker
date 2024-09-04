package ru.smartup.timetracker.core.converter;

import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@AllArgsConstructor
public class StringToEnum<T extends Enum<T>> implements Converter<String, T> {
    private final Class<T> tClass;
    @Override
    public T convert(final String source) {
        if (source.isBlank()) {
            return null;
        }

        return Enum.valueOf(tClass, source);
    }
}
