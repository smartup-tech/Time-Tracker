package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.smartup.timetracker.core.CurrentSessionUserPrincipal;
import ru.smartup.timetracker.core.SessionUserPrincipal;
import ru.smartup.timetracker.dto.report.response.ReportHoursForProjectsDto;
import ru.smartup.timetracker.dto.report.response.ReportHoursForUsersDto;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.service.ReportService;

import java.sql.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reports")
public class ReportRestController {
    private final ReportService reportService;
    private final ModelMapper modelMapper;

    @PreAuthorize("getPrincipal().isManager() or getPrincipal().isReportReceiver() or getPrincipal().isAdmin()")
    @GetMapping("/hoursForProjects")
    public List<ReportHoursForProjectsDto> getReportHoursForProjects(
            @CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
            @RequestParam Date startDate,
            @RequestParam Date endDate) {
        Set<Integer> projectIds = Set.of();
        if (!currentSessionUserPrincipal.isReportReceiver() && !currentSessionUserPrincipal.isAdmin()) {
            projectIds = currentSessionUserPrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER);
        }
        return reportService.getReportHoursForProjects(projectIds, startDate, endDate).stream()
                .map(reportHours -> modelMapper.map(reportHours, ReportHoursForProjectsDto.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isManager() or getPrincipal().isReportReceiver() or getPrincipal().isAdmin()")
    @GetMapping("/hoursForUsers")
    public List<ReportHoursForUsersDto> getReportHoursForUsers(
            @CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
            @RequestParam Date startDate,
            @RequestParam Date endDate) {
        Set<Integer> projectIds = Set.of();
        if (!currentSessionUserPrincipal.isReportReceiver() && !currentSessionUserPrincipal.isAdmin()) {
            projectIds = currentSessionUserPrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER);
        }
        return reportService.getReportHoursForUsers(projectIds, startDate, endDate).stream()
                .map(reportHours -> modelMapper.map(reportHours, ReportHoursForUsersDto.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isUser() or getPrincipal().isAdmin()")
    @GetMapping("/hoursForCurrentUser")
    public List<ReportHoursForUsersDto> getReportHoursForCurrentUser(
            @CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
            @RequestParam Date startDate,
            @RequestParam Date endDate) {
        return reportService.getReportHoursForCurrentUser(currentSessionUserPrincipal.getId(), startDate, endDate).stream()
                .map(reportHours -> modelMapper.map(reportHours, ReportHoursForUsersDto.class))
                .collect(Collectors.toList());
    }
}
