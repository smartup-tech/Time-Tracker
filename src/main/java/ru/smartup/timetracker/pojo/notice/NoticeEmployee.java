package ru.smartup.timetracker.pojo.notice;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class NoticeEmployee {
    private Integer id;

    private String firstName;

    private String lastName;

    private List<EmployeeRoleEnum> roles;

    private ProjectRoleEnum projectRole;

    public NoticeEmployee(ProjectRoleEnum projectRole) {
        this.projectRole = projectRole;
    }

    public NoticeEmployee(Integer id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
