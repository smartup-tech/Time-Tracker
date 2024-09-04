package ru.smartup.timetracker.dto.tracker.response;

import lombok.Data;

import java.sql.Date;

@Data
public class TrackUnitCellDto {
    private long id;

    private Date workDay;

    private float hours;

    private boolean blocked;

    private boolean billable;

    private String comment;

    private String rejectReason;

    private boolean rejected;
}
