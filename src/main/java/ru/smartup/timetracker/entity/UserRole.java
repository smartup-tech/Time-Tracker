package ru.smartup.timetracker.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.entity.field.pk.UserRolePK;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_role", schema = "public")
@IdClass(UserRolePK.class)
public class UserRole {
    @Id
    @Column(name = "user_id")
    private int userId;

    @Id
    @Column(name = "role_id")
    @Enumerated(EnumType.STRING)
    private UserRoleEnum roleId;
}
