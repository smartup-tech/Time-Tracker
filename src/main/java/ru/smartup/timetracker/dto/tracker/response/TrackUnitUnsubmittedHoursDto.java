package ru.smartup.timetracker.dto.tracker.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TrackUnitUnsubmittedHoursDto {
    private LocalDate week;

    private float hours;
}
