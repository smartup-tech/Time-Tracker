package ru.smartup.timetracker.utils;

import lombok.experimental.UtilityClass;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@UtilityClass
public class DateUtils {
    public static final int DAYS_IN_WEEK = 7;
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm z", Locale.ENGLISH);

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

    public static LocalDateTime parseStringDateToLocal(String dateString) {
        return LocalDateTime.parse(dateString, inputFormatter);
    }

    public static String getNextDay(LocalDateTime date) {
        return date.plusDays(1).format(dateFormatter);
    }

}
