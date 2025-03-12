package ru.smartup.timetracker.dto.project.response;

import lombok.Data;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.EmployeeProjectRole;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;

@Data
public class EmployeeInProjectDto {
    private int id;

    private String firstName;

    private String middleName;

    private String lastName;

    private ProjectRoleEnum projectRoleId;

    private Float externalRate;

    //TODO сделать кастомную конфигурацию маппера и убрать туда
    public EmployeeInProjectDto(Employee employee) {
        this.id = employee.getId();
        this.firstName = employee.getFirstName();
        this.middleName = employee.getMiddleName();
        this.lastName = employee.getLastName();
        EmployeeProjectRole employeeProjectRole = employee.getEmployeeProjectRole();
        if (employeeProjectRole != null) {
            this.projectRoleId = employeeProjectRole.getProjectRoleId();
            this.externalRate = employeeProjectRole.getExternalRate();
        }
    }
}
