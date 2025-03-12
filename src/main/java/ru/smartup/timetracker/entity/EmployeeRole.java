package ru.smartup.timetracker.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.pk.EmployeeRolePK;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_role", schema = "public")
@IdClass(EmployeeRolePK.class)
public class EmployeeRole {
    @Id
    @Column(name = "employee_id")
    private int employeeId;

    @Id
    @Column(name = "role_id")
    @Enumerated(EnumType.STRING)
    private EmployeeRoleEnum roleId;
}
