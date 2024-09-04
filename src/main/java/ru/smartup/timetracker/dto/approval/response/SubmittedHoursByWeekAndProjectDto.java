package ru.smartup.timetracker.dto.approval.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.smartup.timetracker.entity.field.enumerated.TrackUnitStatusEnum;

import java.time.LocalDate;

@AllArgsConstructor
@Data
public class SubmittedHoursByWeekAndProjectDto {
    private long trackUnitId;

    private int userId;

    private String firstName;

    private String lastName;

    private long taskId;

    private String taskName;

    private float hours;

    private TrackUnitStatusEnum status;

    private boolean billable;

    private LocalDate workDay;

    private String comment;
}
