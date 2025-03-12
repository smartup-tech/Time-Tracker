package ru.smartup.timetracker.repository;

import ru.smartup.timetracker.pojo.ReportHours;

import java.sql.Date;
import java.util.List;
import java.util.Set;

public interface ReportRepository {
    List<ReportHours> getReportHoursForProjects(Set<Integer> projectIds, Date startDate, Date endDate);

    List<ReportHours> getReportHoursForEmployees(Set<Integer> projectIds, Date startDate, Date endDate);

    List<ReportHours> getReportHoursForCurrentEmployee(int employeeId, Date startDate, Date endDate);
}
