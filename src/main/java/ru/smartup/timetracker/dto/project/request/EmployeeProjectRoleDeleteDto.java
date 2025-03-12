package ru.smartup.timetracker.dto.project.request;

import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class EmployeeProjectRoleDeleteDto {
    @Min(1)
    private int employeeId;
}
