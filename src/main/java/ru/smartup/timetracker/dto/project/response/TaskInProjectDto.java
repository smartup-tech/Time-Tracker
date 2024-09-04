package ru.smartup.timetracker.dto.project.response;

import lombok.Data;

@Data
public class TaskInProjectDto {
    private long id;

    private String name;

    private boolean billable;

    private boolean isArchived;
}
