package ru.smartup.timetracker.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Entity
@Table(name = "task", schema = "public")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "project_id")
    private int projectId;

    @Column(name = "billable")
    private boolean billable;

    @Column(name = "is_archived", insertable = false, updatable = false)
    private boolean isArchived;

    @Column(name = "created_date", insertable = false, updatable = false)
    private Timestamp createdDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Timestamp lastModifiedDate;

    public Task(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
