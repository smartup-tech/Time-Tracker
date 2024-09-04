package ru.smartup.timetracker.utils;

import lombok.experimental.UtilityClass;
import org.springframework.format.datetime.DateFormatter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateUtils {
    public static final int DAYS_IN_WEEK = 7;
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static String formatZoneDate(ZonedDateTime zonedDateTime) {
        StringBuilder time = new StringBuilder();
        time.append(dateTimeFormatter.format(zonedDateTime.toLocalDateTime()));
        time.append(" ");
        time.append(zonedDateTime.getZone().getId());
        return time.toString();
    }

    public static String formatDate(final LocalDate localDate) {
        return dateFormatter.format(localDate);
    }
}
