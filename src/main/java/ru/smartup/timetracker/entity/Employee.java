package ru.smartup.timetracker.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "employee", schema = "public")
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "position_id")
    private int positionId;

    @Column(name = "is_archived", insertable = false, updatable = false)
    private boolean isArchived;

    @Column(name = "created_date", insertable = false, updatable = false)
    private Timestamp createdDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Timestamp lastModifiedDate;

    @ManyToMany
    @JoinTable(
            name = "employee_role",
            joinColumns = {@JoinColumn (name = "employee_id")},
            inverseJoinColumns = {@JoinColumn (name = "role_id")}
    )
    private Set<Role> employeeRoles = new HashSet<>();

    @Transient
    private EmployeeProjectRole employeeProjectRole;

    public Employee(int employeeId, String firstName, String lastName) {
        this.id = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Employee(int projectId, int employeeId, String firstName, String middleName, String lastName,
                    ProjectRoleEnum projectRoleId, Float externalRate) {
        this.id = employeeId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.employeeProjectRole = new EmployeeProjectRole(employeeId, projectId, projectRoleId, externalRate);
    }

}
