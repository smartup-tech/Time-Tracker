package ru.smartup.timetracker.entity.field.pk;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserProjectRolePK implements Serializable {
    private int userId;
    private int projectId;
}