package ru.smartup.timetracker.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "position", schema = "public")
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "external_rate")
    private float externalRate;

    @Column(name = "is_archived", insertable = false, updatable = false)
    private boolean isArchived;

    @Column(name = "created_date", insertable = false, updatable = false)
    private Timestamp createdDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Timestamp lastModifiedDate;
}
