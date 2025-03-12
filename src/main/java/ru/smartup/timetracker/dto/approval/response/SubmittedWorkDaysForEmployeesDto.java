package ru.smartup.timetracker.dto.approval.response;

import lombok.Data;

import java.util.Date;

@Data
public class SubmittedWorkDaysForEmployeesDto {
    private int employeeId;

    private String firstName;

    private String lastName;

    private Date trackUnitWorkDay;

    private int projectId;

    private String projectName;

    private long trackUnitId;

    private long taskId;

    private String taskName;

    private float trackUnitHours;
}
