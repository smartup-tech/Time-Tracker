package ru.smartup.timetracker.dto.report.response;

import lombok.Data;

@Data
public class ReportHoursForProjectsDto {
    private int projectId;

    private String projectName;

    private float billableHours;

    private float billableHoursFrozen;

    private float billableHoursNotFrozen;

    private float unbillableHours;

    private float unbillableHoursFrozen;

    private float unbillableHoursNotFrozen;

    private float totalHours;

    private float totalHoursFrozen;

    private float totalHoursNotFrozen;
}
