package ru.smartup.timetracker.dto.task.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackedProjectTaskDto {
    private int userId;
    private int projectId;
    private String projectName;
    private long taskId;
    private String taskName;
}
