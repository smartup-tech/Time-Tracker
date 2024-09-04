package ru.smartup.timetracker.repository.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import ru.smartup.timetracker.pojo.ReportHours;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class ReportHoursForUsersMapper implements RowMapper<ReportHours> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @SneakyThrows
    @Override
    public ReportHours mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReportHours reportHours = new ReportHours();
        reportHours.setProjectId(rs.getInt("project_id"));
        reportHours.setProjectName(rs.getString("project_name"));
        reportHours.setTaskId(rs.getLong("task_id"));
        reportHours.setTaskName(rs.getString("task_name"));
        reportHours.setUserId(rs.getInt("user_id"));
        reportHours.setUserFirstName(rs.getString("user_first_name"));
        reportHours.setUserLastName(rs.getString("user_last_name"));
        reportHours.setBillableHours(rs.getFloat("billable_hours"));
        reportHours.setBillableHoursFrozen(rs.getFloat("billable_hours_frozen"));
        reportHours.setBillableHoursNotFrozen(rs.getFloat("billable_hours_not_frozen"));
        reportHours.setUnbillableHours(rs.getFloat("unbillable_hours"));
        reportHours.setUnbillableHoursFrozen(rs.getFloat("unbillable_hours_frozen"));
        reportHours.setUnbillableHoursNotFrozen(rs.getFloat("unbillable_hours_not_frozen"));
        reportHours.setTotalHours(rs.getFloat("total_hours"));
        reportHours.setTotalHoursFrozen(rs.getFloat("total_hours_frozen"));
        reportHours.setTotalHoursNotFrozen(rs.getFloat("total_hours_not_frozen"));

        String workHoursMapJson = rs.getString("work_hours_map");
        Map<String, Float> workHoursMap = objectMapper.readValue(workHoursMapJson, new TypeReference<>() {});
        reportHours.setWorkHoursMap(workHoursMap);

        return reportHours;
    }
}