package ru.smartup.timetracker.entity;

import lombok.Data;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;

import javax.persistence.*;

@Data
@Entity
@Table(name = "role")
public class Role {
    @Id
    @Column(name = "id")
    @Enumerated(EnumType.STRING)
    private EmployeeRoleEnum roleId;

    @Column(name = "name")
    private String name;

}