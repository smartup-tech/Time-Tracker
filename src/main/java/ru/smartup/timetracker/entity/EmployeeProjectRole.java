package ru.smartup.timetracker.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.pk.EmployeeProjectRolePK;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_project_role", schema = "public")
@IdClass(EmployeeProjectRolePK.class)
public class EmployeeProjectRole {
    @Id
    @Column(name = "employee_id")
    private int employeeId;

    @Id
    @Column(name = "project_id")
    private int projectId;

    @Column(name = "project_role_id")
    @Enumerated(EnumType.STRING)
    private ProjectRoleEnum projectRoleId;

    @Column(name = "external_rate")
    private Float externalRate;
}
