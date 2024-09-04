package ru.smartup.timetracker.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.smartup.timetracker.entity.field.pk.TrackedProjectTaskPK;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Setter
@Getter
@Table(name = "tracked_project_task")
@IdClass(TrackedProjectTaskPK.class)
public class TrackedProjectTask {
    @Id
    @Column(name = "user_id")
    private int userId;
    @Id
    @Column(name = "task_id")
    private long taskId;
}
