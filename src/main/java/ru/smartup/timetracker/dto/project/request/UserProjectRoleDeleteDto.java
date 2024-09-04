package ru.smartup.timetracker.dto.project.request;

import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class UserProjectRoleDeleteDto {
    @Min(1)
    private int userId;
}
