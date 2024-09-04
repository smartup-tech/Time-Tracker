package ru.smartup.timetracker.entity.field.pk;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserRolePK implements Serializable {
    private int userId;
    private UserRoleEnum roleId;
}
