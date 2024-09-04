package ru.smartup.timetracker.dto.profile.response;

import lombok.Data;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;

import java.util.Set;

@Data
public class ProfileDto {
    private int id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String email;

    private Set<UserRoleEnum> roles;

    private Set<ProjectRoleEnum> projectRoles;
}
