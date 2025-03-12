package ru.smartup.timetracker.dto.employee.response;

import lombok.Data;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;

import java.util.List;

@Data
public class EmployeeShortDto {
    private int id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String email;

    private boolean isArchived;

    private List<EmployeeRoleEnum> roles = List.of();
}
