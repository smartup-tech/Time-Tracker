package ru.smartup.timetracker.dto.tracker.response;

import lombok.Data;

import java.util.List;

@Data
public class TrackUnitTableDto {
    private List<TrackUnitTableDayDto> days;

    private List<TrackUnitRowDto> data;
}
