package ru.smartup.timetracker.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.smartup.timetracker.entity.TrackUnit;
import ru.smartup.timetracker.repository.TrackUnitBatchRepository;

import java.sql.PreparedStatement;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class TrackUnitBatchRepositoryImpl implements TrackUnitBatchRepository {
    private static final int BATCH_SIZE = 100;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void insertOrUpdateHoursAndComment(List<TrackUnit> trackUnits) {
        String query = "INSERT INTO track_unit (user_id, task_id, work_day, hours, comment, status, billable) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (user_id, task_id, work_day) DO UPDATE " +
                "SET hours = excluded.hours, comment = excluded.comment, billable = excluded.billable " +
                "WHERE track_unit.status IN ('CREATED', 'REJECTED') AND track_unit.frozen = false";
        jdbcTemplate.batchUpdate(query, trackUnits, BATCH_SIZE,
                (PreparedStatement ps, TrackUnit trackUnit) -> {
                    ps.setInt(1, trackUnit.getUserId());
                    ps.setLong(2, trackUnit.getTaskId());
                    ps.setDate(3, trackUnit.getWorkDay());
                    ps.setFloat(4, trackUnit.getHours());
                    ps.setString(5, trackUnit.getComment());
                    ps.setString(6, trackUnit.getStatus().name());
                    ps.setBoolean(7, trackUnit.isBillable());
                });
    }

    @Override
    public void deleteTrackUnits(List<TrackUnit> trackUnits) {
        String query = "DELETE FROM track_unit WHERE user_id = ? AND task_id = ? AND work_day = ? " +
                "AND frozen = false AND status IN ('CREATED', 'REJECTED')";
        jdbcTemplate.batchUpdate(query, trackUnits, BATCH_SIZE,
                (PreparedStatement ps, TrackUnit trackUnit) -> {
                    ps.setInt(1, trackUnit.getUserId());
                    ps.setLong(2, trackUnit.getTaskId());
                    ps.setDate(3, trackUnit.getWorkDay());
                });
    }
}