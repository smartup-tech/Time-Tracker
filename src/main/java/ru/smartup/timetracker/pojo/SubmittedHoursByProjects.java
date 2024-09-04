package ru.smartup.timetracker.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubmittedHoursByProjects {
    private int projectId;

    private String projectName;

    private float submittedHours;

    private float totalHours;
}
