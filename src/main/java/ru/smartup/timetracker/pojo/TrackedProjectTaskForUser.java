package ru.smartup.timetracker.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackedProjectTaskForUser {
    private int userId;
    private int projectId;
    private String projectName;
    private long taskId;
    private String taskName;
    private boolean billable;
}
