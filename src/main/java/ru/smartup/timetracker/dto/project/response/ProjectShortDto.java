package ru.smartup.timetracker.dto.project.response;

import lombok.Data;

@Data
public class ProjectShortDto {
    private int id;

    private String name;

    private boolean isArchived;
}
