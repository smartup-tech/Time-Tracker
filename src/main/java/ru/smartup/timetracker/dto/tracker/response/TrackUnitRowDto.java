package ru.smartup.timetracker.dto.tracker.response;

import lombok.Data;

import java.util.List;

@Data
public class TrackUnitRowDto {
    private int userId;

    private int projectId;

    private String projectName;

    private long taskId;

    private String taskName;

    private boolean observed;

    private List<TrackUnitCellDto> units;
}
