package ru.smartup.timetracker.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ReportHours {
    private int projectId;

    private String projectName;

    private long taskId;

    private String taskName;

    private int employeeId;

    private String employeeFirstName;

    private String employeeLastName;

    private float billableHours;

    private float billableHoursFrozen;

    private float billableHoursNotFrozen;

    private float unbillableHours;

    private float unbillableHoursFrozen;

    private float unbillableHoursNotFrozen;

    private float totalHours;

    private float totalHoursFrozen;

    private float totalHoursNotFrozen;

    private Map<String, Float> workHoursMap;
}
