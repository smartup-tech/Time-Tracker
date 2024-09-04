package ru.smartup.timetracker.dto.approval.response;

import lombok.Data;

@Data
public class SubmittedHoursByProjectsDto {
    private int projectId;

    private String projectName;

    private float submittedHours;

    private float totalHours;
}
