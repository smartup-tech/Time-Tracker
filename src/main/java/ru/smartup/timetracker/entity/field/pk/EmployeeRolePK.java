package ru.smartup.timetracker.entity.field.pk;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EmployeeRolePK implements Serializable {
    private int employeeId;
    private EmployeeRoleEnum roleId;
}
