package ru.smartup.timetracker.repository;

import ru.smartup.timetracker.entity.TrackUnit;

import java.util.List;

public interface TrackUnitBatchRepository {
    void insertOrUpdateHoursAndComment(List<TrackUnit> trackUnits);

    void deleteTrackUnits(List<TrackUnit> trackUnits);
}