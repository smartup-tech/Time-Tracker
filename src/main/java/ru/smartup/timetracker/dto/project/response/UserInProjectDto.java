package ru.smartup.timetracker.dto.project.response;

import lombok.Data;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.UserProjectRole;

@Data
public class UserInProjectDto {
    private int id;

    private String firstName;

    private String middleName;

    private String lastName;

    private ProjectRoleEnum projectRoleId;

    private Float externalRate;

    //TODO сделать кастомную конфигурацию маппера и убрать туда
    public UserInProjectDto(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.middleName = user.getMiddleName();
        this.lastName = user.getLastName();
        UserProjectRole userProjectRole = user.getUserProjectRole();
        if (userProjectRole != null) {
            this.projectRoleId = userProjectRole.getProjectRoleId();
            this.externalRate = userProjectRole.getExternalRate();
        }
    }
}
