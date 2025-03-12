package ru.smartup.timetracker.dto.project.request;

import lombok.Data;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class EmployeeProjectRoleModifyDto {
    @Min(1)
    private int employeeId;

    @NotNull
    private ProjectRoleEnum projectRoleId;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    private Float externalRate;
}
