package ru.smartup.timetracker.dto.tracker.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class TrackUnitRowUpdateDto {
    @Min(0)
    private int userId;

    @Min(1)
    private long taskId;

    private boolean observed;

    @Valid
    @NotEmpty
    private List<TrackUnitCellUpdateDto> units;
}
