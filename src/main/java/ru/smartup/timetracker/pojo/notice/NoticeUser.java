package ru.smartup.timetracker.pojo.notice;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class NoticeUser {
    private Integer id;

    private String firstName;

    private String lastName;

    private List<UserRoleEnum> roles;

    private ProjectRoleEnum projectRole;

    public NoticeUser(ProjectRoleEnum projectRole) {
        this.projectRole = projectRole;
    }

    public NoticeUser(Integer id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
