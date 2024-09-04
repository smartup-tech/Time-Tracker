package ru.smartup.timetracker.dto.task.request;

import lombok.Data;
import ru.smartup.timetracker.validation.RequiredField;

import javax.validation.constraints.Min;

@Data
public class TaskCreateDto {
    @RequiredField(maxSize = 255)
    private String name;

    @Min(1)
    private int projectId;

    private boolean billable;
}
