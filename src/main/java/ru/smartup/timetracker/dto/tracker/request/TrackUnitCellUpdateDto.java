package ru.smartup.timetracker.dto.tracker.request;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.sql.Date;

@Data
public class TrackUnitCellUpdateDto {
    @NotNull
    private Date workDay;

    @DecimalMin(value = "0.0")
    @Digits(integer = 2, fraction = 2)
    private float hours;

    private String comment;

    private boolean billable;
}
