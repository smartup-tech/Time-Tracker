package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.smartup.timetracker.core.CurrentSessionEmployeePrincipal;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.dto.report.response.ReportHoursForProjectsDto;
import ru.smartup.timetracker.dto.report.response.ReportHoursForEmployeesDto;
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
            @CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
            @RequestParam Date startDate,
            @RequestParam Date endDate) {
        Set<Integer> projectIds = Set.of();
        if (!currentSessionEmployeePrincipal.isReportReceiver() && !currentSessionEmployeePrincipal.isAdmin()) {
            projectIds = currentSessionEmployeePrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER);
        }
        return reportService.getReportHoursForProjects(projectIds, startDate, endDate).stream()
                .map(reportHours -> modelMapper.map(reportHours, ReportHoursForProjectsDto.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isManager() or getPrincipal().isReportReceiver() or getPrincipal().isAdmin()")
    @GetMapping("/hoursForEmployees")
    public List<ReportHoursForEmployeesDto> getReportHoursForEmployees(
            @CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
            @RequestParam Date startDate,
            @RequestParam Date endDate) {
        Set<Integer> projectIds = Set.of();
        if (!currentSessionEmployeePrincipal.isReportReceiver() && !currentSessionEmployeePrincipal.isAdmin()) {
            projectIds = currentSessionEmployeePrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER);
        }
        return reportService.getReportHoursForEmployees(projectIds, startDate, endDate).stream()
                .map(reportHours -> modelMapper.map(reportHours, ReportHoursForEmployeesDto.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isEmployee() or getPrincipal().isAdmin()")
    @GetMapping("/hoursForCurrentEmployee")
    public List<ReportHoursForEmployeesDto> getReportHoursForCurrentEmployee(
            @CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
            @RequestParam Date startDate,
            @RequestParam Date endDate) {
        return reportService.getReportHoursForCurrentEmployee(currentSessionEmployeePrincipal.getId(), startDate, endDate).stream()
                .map(reportHours -> modelMapper.map(reportHours, ReportHoursForEmployeesDto.class))
                .collect(Collectors.toList());
    }
}
