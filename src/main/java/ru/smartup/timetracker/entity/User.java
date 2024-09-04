package ru.smartup.timetracker.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "user", schema = "public")
@NoArgsConstructor
public class User {
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

    @Transient
    private UserProjectRole userProjectRole;

    public User(int userId, String firstName, String lastName) {
        this.id = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(int projectId, int userId, String firstName, String middleName, String lastName,
                ProjectRoleEnum projectRoleId, Float externalRate) {
        this.id = userId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.userProjectRole = new UserProjectRole(userId, projectId, projectRoleId, externalRate);
    }

}
