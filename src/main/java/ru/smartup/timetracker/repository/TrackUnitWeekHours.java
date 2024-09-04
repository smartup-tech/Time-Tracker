package ru.smartup.timetracker.repository;

import java.time.LocalDate;

public interface TrackUnitWeekHours {
    LocalDate getWeek();

    float getHours();
}
