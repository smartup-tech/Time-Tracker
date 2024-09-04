package ru.smartup.timetracker.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;

@AllArgsConstructor
@Getter
public class ProjectOfUser {
    private int id;

    private String name;

    private boolean archived;

    private Float externalRate;

    private ProjectRoleEnum projectRoleId;
}
