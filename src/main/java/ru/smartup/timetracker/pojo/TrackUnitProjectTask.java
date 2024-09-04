package ru.smartup.timetracker.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackUnitProjectTask {
    private int userId;

    private long trackUnitId;

    private Date trackUnitWorkDay;

    private int projectId;

    private String projectName;

    private long taskId;

    private String taskName;
}
