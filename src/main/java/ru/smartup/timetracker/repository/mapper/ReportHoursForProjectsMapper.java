package ru.smartup.timetracker.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.smartup.timetracker.pojo.ReportHours;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportHoursForProjectsMapper implements RowMapper<ReportHours> {
    @Override
    public ReportHours mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReportHours reportHours = new ReportHours();
        reportHours.setProjectId(rs.getInt("project_id"));
        reportHours.setProjectName(rs.getString("project_name"));
        reportHours.setBillableHours(rs.getFloat("billable_hours"));
        reportHours.setBillableHoursFrozen(rs.getFloat("billable_hours_frozen"));
        reportHours.setBillableHoursNotFrozen(rs.getFloat("billable_hours_not_frozen"));
        reportHours.setUnbillableHours(rs.getFloat("unbillable_hours"));
        reportHours.setUnbillableHoursFrozen(rs.getFloat("unbillable_hours_frozen"));
        reportHours.setUnbillableHoursNotFrozen(rs.getFloat("unbillable_hours_not_frozen"));
        reportHours.setTotalHours(rs.getFloat("total_hours"));
        reportHours.setTotalHoursFrozen(rs.getFloat("total_hours_frozen"));
        reportHours.setTotalHoursNotFrozen(rs.getFloat("total_hours_not_frozen"));
        return reportHours;
    }
}
