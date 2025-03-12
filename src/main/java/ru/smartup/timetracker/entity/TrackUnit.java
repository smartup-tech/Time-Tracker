package ru.smartup.timetracker.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.TrackUnitStatusEnum;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@Entity
@NoArgsConstructor
@Table(name = "track_unit", schema = "public")
public class TrackUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "work_day")
    private Date workDay;

    @Column(name = "status", updatable = false)
    @Enumerated(EnumType.STRING)
    private TrackUnitStatusEnum status;

    @Column(name = "hours")
    private float hours;

    @Column(name = "comment")
    private String comment;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "rate")
    private float rate;

    @Column(name = "billable")
    private boolean billable;

    @Column(name = "task_id")
    private long taskId;

    @Column(name = "employee_id")
    private int employeeId;

    @Column(name = "created_date", insertable = false, updatable = false)
    private Timestamp createdDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Timestamp lastModifiedDate;

    @Column(name = "frozen")
    private boolean frozen;

    @Transient
    private Employee employee;

    @Transient
    private Project project;

    @Transient
    private Task task;

    public TrackUnit(long id, int employeeId, String employeeFirstName, String employeeLastName, long taskId, String taskName,
                     java.util.Date workDay, float hours, TrackUnitStatusEnum status, boolean billable, String comment) {
        this.id = id;
        this.employee = new Employee(employeeId, employeeFirstName, employeeLastName);
        this.task = new Task(taskId, taskName);
        this.workDay = new Date(workDay.getTime());
        this.hours = hours;
        this.status = status;
        this.billable = billable;
        this.comment = comment;
    }

    public TrackUnit(long id, int projectId, String projectName,
                     long taskId, String taskName, java.util.Date workDay,
                     float hours, TrackUnitStatusEnum status, boolean billable, String comment, boolean frozen,
                     String rejectReason) {
        this.id = id;
        this.project = new Project(projectId, projectName);
        this.task = new Task(taskId, taskName);
        this.workDay = new Date(workDay.getTime());
        this.hours = hours;
        this.status = status;
        this.billable = billable;
        this.comment = comment;
        this.frozen = frozen;
        this.rejectReason = rejectReason;
    }
}
