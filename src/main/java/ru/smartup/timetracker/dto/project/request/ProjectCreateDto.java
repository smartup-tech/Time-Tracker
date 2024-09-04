package ru.smartup.timetracker.dto.project.request;

import lombok.Data;
import ru.smartup.timetracker.validation.RequiredField;

@Data
public class ProjectCreateDto {
    @RequiredField(maxSize = 255)
    private String name;
}
