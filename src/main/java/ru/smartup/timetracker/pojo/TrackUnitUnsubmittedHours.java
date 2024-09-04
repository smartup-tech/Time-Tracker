package ru.smartup.timetracker.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TrackUnitUnsubmittedHours {
    private LocalDate week;

    private float hours;
}
