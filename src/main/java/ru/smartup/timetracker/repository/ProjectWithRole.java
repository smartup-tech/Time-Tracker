package ru.smartup.timetracker.repository;

import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;

public interface ProjectWithRole {
    int getId();

    String getName();

    boolean isArchived();

    Float getExternalRate();

    ProjectRoleEnum getProjectRoleId();
}
