package ru.smartup.timetracker.dto.approval.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SubmittedHoursDto {
    private LocalDate week;

    private float hours;
}
