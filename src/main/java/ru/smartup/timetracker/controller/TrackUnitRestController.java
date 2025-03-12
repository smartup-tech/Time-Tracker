package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.core.CurrentSessionEmployeePrincipal;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.dto.EntityDtoConverter;
import ru.smartup.timetracker.dto.project.response.ProjectShortDto;
import ru.smartup.timetracker.dto.tracker.request.TrackUnitRowUpdateDto;
import ru.smartup.timetracker.dto.tracker.request.TrackUnitSubmitDto;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitRowDto;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitTableDto;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitUnsubmittedHoursDto;
import ru.smartup.timetracker.dto.employee.response.EmployeeShortDto;
import ru.smartup.timetracker.entity.*;
import ru.smartup.timetracker.entity.Task;
import ru.smartup.timetracker.entity.TrackedProjectTask;
import ru.smartup.timetracker.entity.TrackUnit;
import ru.smartup.timetracker.entity.field.enumerated.TrackUnitStatusEnum;
import ru.smartup.timetracker.entity.field.sort.EmployeeSortFieldEnum;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.EmployeeProjectRole;
import ru.smartup.timetracker.exception.ForbiddenException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.pojo.TrackUnitUnsubmittedHours;
import ru.smartup.timetracker.service.*;
import ru.smartup.timetracker.service.freeze.CRUDFreezeService;
import ru.smartup.timetracker.utils.CommonStringUtils;
import ru.smartup.timetracker.utils.DateUtils;
import ru.smartup.timetracker.utils.InitBinderUtils;

import javax.validation.Valid;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/trackUnits")
public class TrackUnitRestController {
    private final TrackUnitService trackUnitService;
    private final TaskService taskService;
    private final ProjectService projectService;
    private final EmployeeService employeeService;
    private final CRUDFreezeService CRUDFreezeService;
    private final ObservationTaskService observationTaskService;
    private final ProductionCalendarService productionCalendarService;
    private final ModelMapper modelMapper;

    @InitBinder
    private void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(LocalDate.class, InitBinderUtils.getCustomLocalDateEditor());
    }

    /**
     * Получить проекты в которых текущий пользователь имеет право редактировать записи учета времени у пользователя employeeId
     *
     * @param currentSessionEmployeePrincipal сессионные данные текущего пользователя
     * @param employeeId                      идентификатор пользователя
     * @return List<ProjectShortDto>
     */
    @PreAuthorize("getPrincipal().isEmployee() or getPrincipal().isAdmin()")
    @GetMapping("/projects")
    public List<ProjectShortDto> getProjects(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                                             @RequestParam(defaultValue = "0") int employeeId) {
        List<Project> projects;
        if ((employeeId == 0) || (currentSessionEmployeePrincipal.getId() == employeeId)) {
            if (currentSessionEmployeePrincipal.isAdmin()) {
                projects = projectService.getAllProjects();
            } else {
                projects = projectService.getProjectsByIds(currentSessionEmployeePrincipal.getTrackableProjectIds());
            }
        } else {
            Set<Integer> projectIds = getAvailableProjectIds(employeeId, currentSessionEmployeePrincipal);
            projects = projectService.getProjectsByIds(projectIds);
        }
        return projects.stream()
                .map(project -> modelMapper.map(project, ProjectShortDto.class))
                .collect(Collectors.toList());
    }

    /**
     * Получить пользователей по части имени или фамилии, которыми текущий пользователь может управлять
     * Администратор видит всех
     * Тот кто не является менеджером в любом проекте видит только себя
     * Менеджер видит всех кто входит в проекты, которыми он управляет
     *
     * @param currentSessionEmployeePrincipal сессионные данные текущего пользователя
     * @return List<EmployeeShortDto>
     */
    @PreAuthorize("getPrincipal().isEmployee() or getPrincipal().isAdmin()")
    @GetMapping("/employees")
    public Collection<EmployeeShortDto> searchEmployees(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                                                    @RequestParam(value = "query", defaultValue = StringUtils.EMPTY) String query,
                                                    @RequestParam(value = "archive", defaultValue = "false") boolean archive) {
        Sort sort = Sort.by(Sort.Direction.ASC, EmployeeSortFieldEnum.NAME.getValues());
        if (currentSessionEmployeePrincipal.isAdmin()) {
            Deque<EmployeeShortDto> employeesDto = employeeService.searchEmployees(query, archive, sort).stream()
                    .map(employee -> modelMapper.map(employee, EmployeeShortDto.class))
                    .collect(Collectors.toCollection(ArrayDeque::new));
            putForward(currentSessionEmployeePrincipal.getId(), employeesDto);
            return employeesDto;
        }
        Set<Integer> projectIdsByProjectRole = currentSessionEmployeePrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER);
        if (!currentSessionEmployeePrincipal.isManager() || projectIdsByProjectRole.isEmpty()) {
            Optional<Employee> existEmployee = employeeService.getEmployee(currentSessionEmployeePrincipal.getId());
            if (existEmployee.isEmpty()) {
                throw new ResourceNotFoundException("Employee was not found by employeeId = "
                        + currentSessionEmployeePrincipal.getId() + ".");
            }
            return List.of(modelMapper.map(existEmployee.get(), EmployeeShortDto.class));
        }
        Deque<EmployeeShortDto> employeesDto = employeeService.searchEmployeesFromProjects(
                projectIdsByProjectRole, CommonStringUtils.escapePercentAndUnderscore(query), archive, sort)
                .stream()
                .map(employee -> modelMapper.map(employee, EmployeeShortDto.class))
                .collect(Collectors.toCollection(ArrayDeque::new));
        putForward(currentSessionEmployeePrincipal.getId(), employeesDto);
        return employeesDto;
    }

    private void putForward(int employeeId, Deque<EmployeeShortDto> employeesDto) {
        employeesDto.stream()
                .filter(employeeShortDto -> employeeShortDto.getId() == employeeId)
                .findFirst()
                .ifPresent(employeeShortDto -> {
                    employeesDto.remove(employeeShortDto);
                    employeesDto.addFirst(employeeShortDto);
                });
    }

    /**
     * Получить записи пользователя с employeeId за неделю, которой принадлежит dateOfWeek.
     * Администратор (может обновлять/просматривать у всех пользователей)
     * Работник (может обновлять/просматривать свои)
     * Менеджер (может обновлять/просматривать свои и те, которые относятся к управляемым им проектам)
     *
     * @return TrackUnitTableDto
     */
    @PreAuthorize("getPrincipal().isEmployee() or getPrincipal().isAdmin()")
    @GetMapping("/week")
    public TrackUnitTableDto getDataForWeek(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                                            @RequestParam(defaultValue = "0") int employeeId,
                                            @RequestParam(name = "dateWeek", defaultValue = "now()") LocalDate dateOfWeek) {
        if (employeeId == 0) {
            employeeId = currentSessionEmployeePrincipal.getId();
        }

        LocalDate firstDayOfWeek = dateOfWeek.with(DayOfWeek.MONDAY);
        List<ProductionCalendarDay> calendarDays = productionCalendarService.getAllProductionCalendarDayByYear(firstDayOfWeek.getYear());

        if ((currentSessionEmployeePrincipal.getId() == employeeId) || currentSessionEmployeePrincipal.isAdmin()) {
            return EntityDtoConverter.getTrackUnitTableDto(employeeId,
                    trackUnitService.getByEmployeeIdAndRange(employeeId, firstDayOfWeek,
                            firstDayOfWeek.plusDays(DateUtils.DAYS_IN_WEEK - 1)), calendarDays, firstDayOfWeek, modelMapper,
                    CRUDFreezeService.getCacheableLastFreeze(), observationTaskService.getTrackedProjectTaskInfoByEmployee(employeeId));
        }

        Set<Integer> employeeProjectIds = employeeService.getEmployeeProjectRoles(employeeId).stream()
                .map(EmployeeProjectRole::getProjectId)
                .collect(Collectors.toSet());

        employeeProjectIds.retainAll(currentSessionEmployeePrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER));

        List<TrackUnit> trackUnits = employeeProjectIds.isEmpty() ? List.of()
                : trackUnitService.getByEmployeeIdAndProjectIdsAndRange(employeeId, employeeProjectIds,
                firstDayOfWeek, firstDayOfWeek.plusDays(DateUtils.DAYS_IN_WEEK - 1));

        return EntityDtoConverter.getTrackUnitTableDto(employeeId, trackUnits, calendarDays, firstDayOfWeek, modelMapper,
                CRUDFreezeService.getCacheableLastFreeze());
    }

    @PreAuthorize("getPrincipal().isEmployee() or getPrincipal().isAdmin()")
    @PatchMapping("/week")
    public TrackUnitRowDto updateDataForWeek(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                                             @Valid @RequestBody TrackUnitRowUpdateDto trackUnitRowUpdateDto) {
        return updateOrDeleteDataForWeek(currentSessionEmployeePrincipal, trackUnitRowUpdateDto, false);
    }

    @PreAuthorize("getPrincipal().isEmployee() or getPrincipal().isAdmin()")
    @DeleteMapping("/week")
    public void deleteDataForWeek(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                                  @Valid @RequestBody TrackUnitRowUpdateDto trackUnitRowUpdateDto) {
        updateOrDeleteDataForWeek(currentSessionEmployeePrincipal, trackUnitRowUpdateDto, true);
    }

    /**
     * Удалить часы или обновить часы и другие данные в трекерах, при отсутствии создать.
     * EMPLOYEE может создать для себя или обновить свои записи
     * MANAGER может создать или обновить записи любого пользователя в своем проекте
     * Администратор имеет полный доступ
     *
     * @param currentSessionEmployeePrincipal сессионные данные текущего пользователя
     * @param trackUnitRowUpdateDto       недельные записи по задаче
     * @return TrackUnitRowDto
     */
    private TrackUnitRowDto updateOrDeleteDataForWeek(SessionEmployeePrincipal currentSessionEmployeePrincipal,
                                                      TrackUnitRowUpdateDto trackUnitRowUpdateDto,
                                                      boolean delete) {
        Optional<Task> existTask = taskService.getNotArchivedTask(trackUnitRowUpdateDto.getTaskId());
        if (existTask.isEmpty()) {
            throw new ResourceNotFoundException("Active task was not found by taskId = "
                    + trackUnitRowUpdateDto.getTaskId() + ".");
        }
        Task task = existTask.get();

        Optional<Project> existProject = projectService.getProject(task.getProjectId());
        if (existProject.isEmpty()) {
            throw new ResourceNotFoundException("Project was not found by projectId = " + task.getProjectId() + ".");
        }
        Project project = existProject.get();

        int employeeId = (trackUnitRowUpdateDto.getEmployeeId() == 0)
                ? currentSessionEmployeePrincipal.getId() : trackUnitRowUpdateDto.getEmployeeId();
        if (!(((employeeId == currentSessionEmployeePrincipal.getId())
                && currentSessionEmployeePrincipal.isEmployee(task.getProjectId()))
                || currentSessionEmployeePrincipal.isManager(task.getProjectId())
                || currentSessionEmployeePrincipal.isAdmin())) {
            throw new ForbiddenException("Employee has not admin, manager or employee role in project; employeeId = "
                    + currentSessionEmployeePrincipal.getId() + ", projectId = " + task.getProjectId() + ".");
        }
        List<TrackUnit> trackUnits = trackUnitRowUpdateDto.getUnits().stream()
                .map(trackUnitCellUpdateDto -> {
                    TrackUnit trackUnit = modelMapper.map(trackUnitCellUpdateDto, TrackUnit.class);
                    trackUnit.setEmployeeId(employeeId);
                    trackUnit.setTaskId(trackUnitRowUpdateDto.getTaskId());
                    trackUnit.setStatus(TrackUnitStatusEnum.CREATED);
                    return trackUnit;
                })
                .collect(Collectors.toList());

        if (delete) {
            trackUnitService.deleteTrackUnits(trackUnits);
            observationTaskService.removeObservationForTask(employeeId, trackUnitRowUpdateDto.getTaskId());
            return null;
        }

        LocalDate firstDayOfWeek = trackUnitRowUpdateDto.getUnits().get(0).getWorkDay()
                .toLocalDate().with(DayOfWeek.MONDAY);
        FreezeRecord maxFreezeDate = CRUDFreezeService.getCacheableLastFreeze();
        trackUnitService.insertOrUpdateHoursAndComment(trackUnits, maxFreezeDate == null ? null : maxFreezeDate.getFreezeDate());

        Optional<TrackedProjectTask> trackedProjectTask = observationTaskService.getTrackedProjectTaskByEmployeeIdAndTaskId(employeeId, trackUnitRowUpdateDto.getTaskId());
        boolean observed = trackedProjectTask.isPresent();
        if (trackedProjectTask.isEmpty() && trackUnitRowUpdateDto.isObserved()) {
            observationTaskService.observeTask(modelMapper.map(trackUnitRowUpdateDto, TrackedProjectTask.class));
            observed = true;
        } else if (trackedProjectTask.isPresent() && !trackUnitRowUpdateDto.isObserved()) {
            observationTaskService.removeObservationForTask(employeeId, trackedProjectTask.get().getTaskId());
            observed = false;
        }

        List<TrackUnit> trackUnitList = trackUnitService.getByEmployeeIdAndTaskIdAndRange(employeeId,
                trackUnitRowUpdateDto.getTaskId(), firstDayOfWeek, firstDayOfWeek.plusDays(DateUtils.DAYS_IN_WEEK - 1));
        return EntityDtoConverter.getTrackUnitRowDto(employeeId, project, task, trackUnitList,
                firstDayOfWeek, modelMapper, maxFreezeDate, observed);
    }

    @PreAuthorize("getPrincipal().isEmployee() or getPrincipal().isAdmin()")
    @GetMapping("/unsubmitted")
    public List<TrackUnitUnsubmittedHoursDto> getUnsubmittedHours(
            @CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
            @RequestParam(defaultValue = "0") int employeeId
    ) {
        if (employeeId == 0) {
            employeeId = currentSessionEmployeePrincipal.getId();
        }
        List<TrackUnitUnsubmittedHours> hours;
        if ((currentSessionEmployeePrincipal.getId() == employeeId) || currentSessionEmployeePrincipal.isAdmin()) {
            hours = trackUnitService.getUnsubmittedHours(employeeId);
        } else {
            Set<Integer> projectIds = getAvailableProjectIds(employeeId, currentSessionEmployeePrincipal);
            hours = trackUnitService.getUnsubmittedHours(employeeId, projectIds);
        }
        return hours.stream()
                .map(unsubmittedHours -> modelMapper.map(unsubmittedHours, TrackUnitUnsubmittedHoursDto.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isEmployee() or getPrincipal().isAdmin()")
    @PostMapping("/submit")
    public void submitHours(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                            @Valid @RequestBody TrackUnitSubmitDto submitDto) {
        int employeeId = submitDto.getEmployeeId() == 0 ? currentSessionEmployeePrincipal.getId() : submitDto.getEmployeeId();
        if ((currentSessionEmployeePrincipal.getId() == employeeId) || currentSessionEmployeePrincipal.isAdmin()) {
            trackUnitService.submit(employeeId, submitDto.getWeeks().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } else {
            Set<Integer> projectIds = getAvailableProjectIds(employeeId, currentSessionEmployeePrincipal);
            trackUnitService.submit(employeeId, projectIds, submitDto.getWeeks().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
    }

    private Set<Integer> getAvailableProjectIds(int employeeId, SessionEmployeePrincipal currentSessionEmployeePrincipal) {
        Set<Integer> projectIds = employeeService.getEmployeeProjectRoles(employeeId).stream()
                .filter(employeeProjectRole -> employeeProjectRole.getProjectRoleId().equals(ProjectRoleEnum.MANAGER)
                        || employeeProjectRole.getProjectRoleId().equals(ProjectRoleEnum.EMPLOYEE))
                .map(EmployeeProjectRole::getProjectId)
                .collect(Collectors.toSet());
        if (!currentSessionEmployeePrincipal.isAdmin()) {
            projectIds.retainAll(currentSessionEmployeePrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER));
            if (projectIds.isEmpty()) {
                throw new ForbiddenException("Employee has not admin or manager role in projects of requested employee;" +
                        " employeeId = " + employeeId + ", currentEmployeeId = " + currentSessionEmployeePrincipal.getId() + ".");
            }
        }
        return projectIds;
    }
}
