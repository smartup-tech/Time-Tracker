package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.core.CurrentSessionEmployeePrincipal;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.dto.EntityDtoConverter;
import ru.smartup.timetracker.dto.approval.request.SubmittedHoursApproveDto;
import ru.smartup.timetracker.dto.approval.request.SubmittedHoursRejectDto;
import ru.smartup.timetracker.dto.approval.response.SubmittedHoursByProjectsDto;
import ru.smartup.timetracker.dto.approval.response.SubmittedHoursByWeekAndProjectDto;
import ru.smartup.timetracker.dto.approval.response.SubmittedHoursDto;
import ru.smartup.timetracker.dto.approval.response.SubmittedWorkDaysTableDto;
import ru.smartup.timetracker.entity.*;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.Task;
import ru.smartup.timetracker.entity.TrackUnit;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.exception.ForbiddenException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.pojo.SubmittedHours;
import ru.smartup.timetracker.pojo.SubmittedHoursByProjects;
import ru.smartup.timetracker.pojo.SubmittedWorkDaysForEmployees;
import ru.smartup.timetracker.service.ProductionCalendarService;
import ru.smartup.timetracker.service.ProjectService;
import ru.smartup.timetracker.service.TrackUnitService;
import ru.smartup.timetracker.service.freeze.CRUDFreezeService;
import ru.smartup.timetracker.utils.InitBinderUtils;

import javax.validation.Valid;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/approval")
public class ApprovalRestController {
    private final TrackUnitService trackUnitService;
    private final ProjectService projectService;
    private final ProductionCalendarService productionCalendarService;
    private final CRUDFreezeService CRUDFreezeService;
    private final ModelMapper modelMapper;

    @InitBinder
    private void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(LocalDate.class, InitBinderUtils.getCustomLocalDateEditor());
    }

    @PreAuthorize("getPrincipal().isManager() or getPrincipal().isAdmin()")
    @GetMapping("/submitted")
    public List<SubmittedHoursDto> getSubmittedHours(
            @CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal
    ) {
        List<SubmittedHours> hours;
        if (currentSessionEmployeePrincipal.isAdmin()) {
            hours = trackUnitService.getSubmittedHours();
        } else {
            Set<Integer> projectIds = currentSessionEmployeePrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER);
            if (projectIds.isEmpty()) {
                throw new ForbiddenException("Employee has not admin or manager role in any project; employeeId = "
                        + currentSessionEmployeePrincipal.getId() + ".");
            } else {
                hours = trackUnitService.getSubmittedHours(projectIds);
            }
        }
        return hours.stream()
                .map(submittedHours -> modelMapper.map(submittedHours, SubmittedHoursDto.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isManager() or getPrincipal().isAdmin()")
    @GetMapping("/submittedByProjects")
    public List<SubmittedHoursByProjectsDto> getSubmittedHoursByProjects(
            @CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
            @RequestParam(name = "dateWeek", defaultValue = "now()") LocalDate dateOfWeek
    ) {
        LocalDate firstDayOfWeek = dateOfWeek.with(DayOfWeek.MONDAY);
        List<SubmittedHoursByProjects> hours;
        if (currentSessionEmployeePrincipal.isAdmin()) {
            hours = trackUnitService.getSubmittedHoursByProjects(Date.valueOf(firstDayOfWeek));
        } else {
            Set<Integer> projectIds = currentSessionEmployeePrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER);
            if (projectIds.isEmpty()) {
                throw new ForbiddenException("Employee has not admin or manager role in any project; employeeId = "
                        + currentSessionEmployeePrincipal.getId() + ".");
            } else {
                hours = trackUnitService.getSubmittedHoursByProjects(Date.valueOf(firstDayOfWeek), projectIds);
            }
        }
        return hours.stream()
                .map(submittedHours -> modelMapper.map(submittedHours, SubmittedHoursByProjectsDto.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isManager() or getPrincipal().isAdmin()")
    @GetMapping("/submittedDays")
    public SubmittedWorkDaysTableDto getSubmittedWorkDay(
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            final @RequestParam(value = "endDate", defaultValue = "now()") LocalDate endDate,
            final @CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal
    ) {
        List<SubmittedWorkDaysForEmployees> workDaysStatistics;
        startDate = startDate == null ? CRUDFreezeService.getCacheableLastFreeze().getFreezeDate() : startDate;
        if (currentSessionEmployeePrincipal.isAdmin()) {
            workDaysStatistics = trackUnitService.getSubmittedHoursForEmployee(startDate, endDate);
        } else {
            final Set<Integer> projectIds = currentSessionEmployeePrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER);
            if (projectIds.isEmpty()) {
                throw new ForbiddenException("Employee has not admin or manager role in any project; employeeId = "
                        + currentSessionEmployeePrincipal.getId() + ".");
            } else {
                workDaysStatistics = trackUnitService.getSubmittedHoursForEmployee(projectIds, startDate, endDate);
            }
        }

        final List<ProductionCalendarDay> calendarDays = productionCalendarService.getAllProductionCalendarDay();

        return EntityDtoConverter.getSubmittedWorkDaysTableDto(workDaysStatistics, calendarDays);
    }

    @PreAuthorize("getPrincipal().isManager(#projectId) or getPrincipal().isAdmin()")
    @GetMapping("/submitted/{projectId}")
    public List<SubmittedHoursByWeekAndProjectDto> getSubmittedHoursForApproval(
            @RequestParam(name = "dateWeek", defaultValue = "now()") LocalDate dateOfWeek,
            @PathVariable(name = "projectId") int projectId
    ) {
        LocalDate firstDayOfWeek = dateOfWeek.with(DayOfWeek.MONDAY);
        Optional<Project> existProject = projectService.getNotArchivedProject(projectId);
        if (existProject.isEmpty()) {
            throw new ResourceNotFoundException("Project was not found by projectId = " + projectId + ".");
        }
        return trackUnitService.getSubmittedHoursByWeekAndProjectId(Date.valueOf(firstDayOfWeek), projectId).stream()
                .map(this::createSubmittedHoursByWeekAndProject)
                .collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isManager() or getPrincipal().isAdmin()")
    @PostMapping("/approve")
    public void approveHours(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                             @Valid @RequestBody SubmittedHoursApproveDto approveDto) {
        List<Long> trackUnitIds = approveDto.getTrackUnitIds();
        if (currentSessionEmployeePrincipal.isAdmin()) {
            trackUnitService.approve(trackUnitIds);
        } else {
            Set<Integer> projectIds = currentSessionEmployeePrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER);
            Set<Integer> trackUnitProjectIds = trackUnitService.getProjectIdsForTrackUnits(trackUnitIds);
            if (!trackUnitProjectIds.isEmpty() && !projectIds.containsAll(trackUnitProjectIds)) {
                throw new ForbiddenException("Employee has not admin or manager role in projects of specified track units; " +
                        "employeeId = " + currentSessionEmployeePrincipal.getId() + ", trackUnitProjectIds = "
                        + trackUnitProjectIds + ".");
            }
            if (!trackUnitProjectIds.isEmpty()) {
                trackUnitService.approve(trackUnitIds);
            }
        }
    }

    @PreAuthorize("getPrincipal().isManager() or getPrincipal().isAdmin()")
    @PostMapping("/reject")
    public void rejectHours(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                            @Valid @RequestBody SubmittedHoursRejectDto rejectDto) {
        List<Long> trackUnitIds = rejectDto.getTrackUnitIds();
        if (currentSessionEmployeePrincipal.isAdmin()) {
            trackUnitService.reject(trackUnitIds, rejectDto.getRejectReason());
        } else {
            Set<Integer> projectIds = currentSessionEmployeePrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER);
            Set<Integer> trackUnitProjectIds = trackUnitService.getProjectIdsForTrackUnits(trackUnitIds);
            if (!trackUnitProjectIds.isEmpty() && !projectIds.containsAll(trackUnitProjectIds)) {
                throw new ForbiddenException("Employee has not admin or manager role in projects of specified track units; " +
                        "employeeId = " + currentSessionEmployeePrincipal.getId() + ", trackUnitProjectIds = "
                        + trackUnitProjectIds + ".");
            }
            if (!trackUnitProjectIds.isEmpty()) {
                trackUnitService.reject(trackUnitIds, rejectDto.getRejectReason());
            }
        }
    }

    private SubmittedHoursByWeekAndProjectDto createSubmittedHoursByWeekAndProject(TrackUnit trackUnit) {
        Employee employee = trackUnit.getEmployee();
        Task task = trackUnit.getTask();
        return new SubmittedHoursByWeekAndProjectDto(trackUnit.getId(), employee.getId(), employee.getFirstName(),
                employee.getLastName(), task.getId(), task.getName(), trackUnit.getHours(), trackUnit.getStatus(),
                trackUnit.isBillable(), trackUnit.getWorkDay().toLocalDate(), trackUnit.getComment());
    }

}
