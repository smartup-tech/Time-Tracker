package ru.smartup.timetracker.dto.profile.response;

import lombok.Data;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;

import java.util.Set;

@Data
public class ProfileDto {
    private int id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String email;

    private Set<EmployeeRoleEnum> roles;

    private Set<ProjectRoleEnum> projectRoles;
}
