package ru.smartup.timetracker.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@AllArgsConstructor
@Getter
public class SubmittedWorkDaysForUsers {
    private int userId;

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
