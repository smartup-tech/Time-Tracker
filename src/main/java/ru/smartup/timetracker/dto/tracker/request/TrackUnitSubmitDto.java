package ru.smartup.timetracker.dto.tracker.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.sql.Date;
import java.util.Set;

@Data
public class TrackUnitSubmitDto {
    @Min(0)
    private int userId;

    @NotEmpty
    private Set<Date> weeks;
}
