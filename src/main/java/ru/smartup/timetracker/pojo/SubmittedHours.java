package ru.smartup.timetracker.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SubmittedHours {
    private LocalDate week;

    private float hours;
}
