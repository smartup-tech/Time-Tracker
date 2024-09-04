package ru.smartup.timetracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.smartup.timetracker.pojo.ReportHours;
import ru.smartup.timetracker.repository.ReportRepository;

import java.sql.Date;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class ReportService {
    private final ReportRepository reportRepository;

    public List<ReportHours> getReportHoursForProjects(Set<Integer> projectIds, Date startDate, Date endDate) {
        return reportRepository.getReportHoursForProjects(projectIds, startDate, endDate);
    }

    public List<ReportHours> getReportHoursForUsers(Set<Integer> projectIds, Date startDate, Date endDate) {
        return reportRepository.getReportHoursForUsers(projectIds, startDate, endDate);
    }

    public List<ReportHours> getReportHoursForCurrentUser(int userId, Date startDate, Date endDate) {
        return reportRepository.getReportHoursForCurrentUser(userId, startDate, endDate);
    }
}
