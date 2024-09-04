package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.core.CurrentSessionUserPrincipal;
import ru.smartup.timetracker.core.SessionUserPrincipal;
import ru.smartup.timetracker.dto.EntityDtoConverter;
import ru.smartup.timetracker.dto.project.response.ProjectShortDto;
import ru.smartup.timetracker.dto.tracker.request.TrackUnitRowUpdateDto;
import ru.smartup.timetracker.dto.tracker.request.TrackUnitSubmitDto;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitRowDto;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitTableDto;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitUnsubmittedHoursDto;
import ru.smartup.timetracker.dto.user.response.UserShortDto;
import ru.smartup.timetracker.entity.*;
import ru.smartup.timetracker.entity.Task;
import ru.smartup.timetracker.entity.TrackedProjectTask;
import ru.smartup.timetracker.entity.TrackUnit;
import ru.smartup.timetracker.entity.field.enumerated.TrackUnitStatusEnum;
import ru.smartup.timetracker.entity.field.sort.UserSortFieldEnum;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.UserProjectRole;
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
    private final UserService userService;
    private final CRUDFreezeService CRUDFreezeService;
    private final ObservationTaskService observationTaskService;
    private final ProductionCalendarService productionCalendarService;
    private final ModelMapper modelMapper;

    @InitBinder
    private void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(LocalDate.class, InitBinderUtils.getCustomLocalDateEditor());
    }

    /**
     * Получить проекты в которых текущий пользователь имеет право редактировать записи учета времени у пользователя userId
     *
     * @param currentSessionUserPrincipal сессионные данные текущего пользователя
     * @param userId                      идентификатор пользователя
     * @return List<ProjectShortDto>
     */
    @PreAuthorize("getPrincipal().isUser() or getPrincipal().isAdmin()")
    @GetMapping("/projects")
    public List<ProjectShortDto> getProjects(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
                                             @RequestParam(defaultValue = "0") int userId) {
        List<Project> projects;
        if ((userId == 0) || (currentSessionUserPrincipal.getId() == userId)) {
            if (currentSessionUserPrincipal.isAdmin()) {
                projects = projectService.getAllProjects();
            } else {
                projects = projectService.getProjectsByIds(currentSessionUserPrincipal.getTrackableProjectIds());
            }
        } else {
            Set<Integer> projectIds = getAvailableProjectIds(userId, currentSessionUserPrincipal);
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
     * @param currentSessionUserPrincipal сессионные данные текущего пользователя
     * @return List<UserShortDto>
     */
    @PreAuthorize("getPrincipal().isUser() or getPrincipal().isAdmin()")
    @GetMapping("/users")
    public Collection<UserShortDto> searchUsers(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
                                                @RequestParam(value = "query", defaultValue = StringUtils.EMPTY) String query,
                                                @RequestParam(value = "archive", defaultValue = "false") boolean archive) {
        Sort sort = Sort.by(Sort.Direction.ASC, UserSortFieldEnum.NAME.getValues());
        if (currentSessionUserPrincipal.isAdmin()) {
            Deque<UserShortDto> usersDto = userService.searchUsers(query, archive, sort).stream()
                    .map(user -> modelMapper.map(user, UserShortDto.class))
                    .collect(Collectors.toCollection(ArrayDeque::new));
            putForward(currentSessionUserPrincipal.getId(), usersDto);
            return usersDto;
        }
        Set<Integer> projectIdsByProjectRole = currentSessionUserPrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER);
        if (!currentSessionUserPrincipal.isManager() || projectIdsByProjectRole.isEmpty()) {
            Optional<User> existUser = userService.getUser(currentSessionUserPrincipal.getId());
            if (existUser.isEmpty()) {
                throw new ResourceNotFoundException("User was not found by userId = "
                        + currentSessionUserPrincipal.getId() + ".");
            }
            return List.of(modelMapper.map(existUser.get(), UserShortDto.class));
        }
        Deque<UserShortDto> usersDto = userService.searchUsersFromProjects(
                projectIdsByProjectRole, CommonStringUtils.escapePercentAndUnderscore(query), archive, sort)
                .stream()
                .map(user -> modelMapper.map(user, UserShortDto.class))
                .collect(Collectors.toCollection(ArrayDeque::new));
        putForward(currentSessionUserPrincipal.getId(), usersDto);
        return usersDto;
    }

    private void putForward(int userId, Deque<UserShortDto> usersDto) {
        usersDto.stream()
                .filter(userShortDto -> userShortDto.getId() == userId)
                .findFirst()
                .ifPresent(userShortDto -> {
                    usersDto.remove(userShortDto);
                    usersDto.addFirst(userShortDto);
                });
    }

    /**
     * Получить записи пользователя с userId за неделю, которой принадлежит dateOfWeek.
     * Администратор (может обновлять/просматривать у всех пользователей)
     * Работник (может обновлять/просматривать свои)
     * Менеджер (может обновлять/просматривать свои и те, которые относятся к управляемым им проектам)
     *
     * @return TrackUnitTableDto
     */
    @PreAuthorize("getPrincipal().isUser() or getPrincipal().isAdmin()")
    @GetMapping("/week")
    public TrackUnitTableDto getDataForWeek(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
                                            @RequestParam(defaultValue = "0") int userId,
                                            @RequestParam(name = "dateWeek", defaultValue = "now()") LocalDate dateOfWeek) {
        if (userId == 0) {
            userId = currentSessionUserPrincipal.getId();
        }

        LocalDate firstDayOfWeek = dateOfWeek.with(DayOfWeek.MONDAY);
        List<ProductionCalendarDay> calendarDays = productionCalendarService.getAllProductionCalendarDayByYear(firstDayOfWeek.getYear());

        if ((currentSessionUserPrincipal.getId() == userId) || currentSessionUserPrincipal.isAdmin()) {
            return EntityDtoConverter.getTrackUnitTableDto(userId,
                    trackUnitService.getByUserIdAndRange(userId, firstDayOfWeek,
                            firstDayOfWeek.plusDays(DateUtils.DAYS_IN_WEEK - 1)), calendarDays, firstDayOfWeek, modelMapper,
                    CRUDFreezeService.getCacheableLastFreeze(), observationTaskService.getTrackedProjectTaskInfoByUser(userId));
        }

        Set<Integer> userProjectIds = userService.getUserProjectRoles(userId).stream()
                .map(UserProjectRole::getProjectId)
                .collect(Collectors.toSet());

        userProjectIds.retainAll(currentSessionUserPrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER));

        List<TrackUnit> trackUnits = userProjectIds.isEmpty() ? List.of()
                : trackUnitService.getByUserIdAndProjectIdsAndRange(userId, userProjectIds,
                firstDayOfWeek, firstDayOfWeek.plusDays(DateUtils.DAYS_IN_WEEK - 1));

        return EntityDtoConverter.getTrackUnitTableDto(userId, trackUnits, calendarDays, firstDayOfWeek, modelMapper,
                CRUDFreezeService.getCacheableLastFreeze());
    }

    @PreAuthorize("getPrincipal().isUser() or getPrincipal().isAdmin()")
    @PatchMapping("/week")
    public TrackUnitRowDto updateDataForWeek(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
                                             @Valid @RequestBody TrackUnitRowUpdateDto trackUnitRowUpdateDto) {
        return updateOrDeleteDataForWeek(currentSessionUserPrincipal, trackUnitRowUpdateDto, false);
    }

    @PreAuthorize("getPrincipal().isUser() or getPrincipal().isAdmin()")
    @DeleteMapping("/week")
    public void deleteDataForWeek(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
                                  @Valid @RequestBody TrackUnitRowUpdateDto trackUnitRowUpdateDto) {
        updateOrDeleteDataForWeek(currentSessionUserPrincipal, trackUnitRowUpdateDto, true);
    }

    /**
     * Удалить часы или обновить часы и другие данные в трекерах, при отсутствии создать.
     * EMPLOYEE может создать для себя или обновить свои записи
     * MANAGER может создать или обновить записи любого пользователя в своем проекте
     * Администратор имеет полный доступ
     *
     * @param currentSessionUserPrincipal сессионные данные текущего пользователя
     * @param trackUnitRowUpdateDto       недельные записи по задаче
     * @return TrackUnitRowDto
     */
    private TrackUnitRowDto updateOrDeleteDataForWeek(SessionUserPrincipal currentSessionUserPrincipal,
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

        int userId = (trackUnitRowUpdateDto.getUserId() == 0)
                ? currentSessionUserPrincipal.getId() : trackUnitRowUpdateDto.getUserId();
        if (!(((userId == currentSessionUserPrincipal.getId())
                && currentSessionUserPrincipal.isEmployee(task.getProjectId()))
                || currentSessionUserPrincipal.isManager(task.getProjectId())
                || currentSessionUserPrincipal.isAdmin())) {
            throw new ForbiddenException("User has not admin, manager or employee role in project; userId = "
                    + currentSessionUserPrincipal.getId() + ", projectId = " + task.getProjectId() + ".");
        }
        List<TrackUnit> trackUnits = trackUnitRowUpdateDto.getUnits().stream()
                .map(trackUnitCellUpdateDto -> {
                    TrackUnit trackUnit = modelMapper.map(trackUnitCellUpdateDto, TrackUnit.class);
                    trackUnit.setUserId(userId);
                    trackUnit.setTaskId(trackUnitRowUpdateDto.getTaskId());
                    trackUnit.setStatus(TrackUnitStatusEnum.CREATED);
                    return trackUnit;
                })
                .collect(Collectors.toList());

        if (delete) {
            trackUnitService.deleteTrackUnits(trackUnits);
            observationTaskService.removeObservationForTask(userId, trackUnitRowUpdateDto.getTaskId());
            return null;
        }

        LocalDate firstDayOfWeek = trackUnitRowUpdateDto.getUnits().get(0).getWorkDay()
                .toLocalDate().with(DayOfWeek.MONDAY);
        FreezeRecord maxFreezeDate = CRUDFreezeService.getCacheableLastFreeze();
        trackUnitService.insertOrUpdateHoursAndComment(trackUnits, maxFreezeDate == null ? null : maxFreezeDate.getFreezeDate());

        Optional<TrackedProjectTask> trackedProjectTask = observationTaskService.getTrackedProjectTaskByUserIdAndTaskId(userId, trackUnitRowUpdateDto.getTaskId());
        boolean observed = trackedProjectTask.isPresent();
        if (trackedProjectTask.isEmpty() && trackUnitRowUpdateDto.isObserved()) {
            observationTaskService.observeTask(modelMapper.map(trackUnitRowUpdateDto, TrackedProjectTask.class));
            observed = true;
        } else if (trackedProjectTask.isPresent() && !trackUnitRowUpdateDto.isObserved()) {
            observationTaskService.removeObservationForTask(userId, trackedProjectTask.get().getTaskId());
            observed = false;
        }

        List<TrackUnit> trackUnitList = trackUnitService.getByUserIdAndTaskIdAndRange(userId,
                trackUnitRowUpdateDto.getTaskId(), firstDayOfWeek, firstDayOfWeek.plusDays(DateUtils.DAYS_IN_WEEK - 1));
        return EntityDtoConverter.getTrackUnitRowDto(userId, project, task, trackUnitList,
                firstDayOfWeek, modelMapper, maxFreezeDate, observed);
    }

    @PreAuthorize("getPrincipal().isUser() or getPrincipal().isAdmin()")
    @GetMapping("/unsubmitted")
    public List<TrackUnitUnsubmittedHoursDto> getUnsubmittedHours(
            @CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
            @RequestParam(defaultValue = "0") int userId
    ) {
        if (userId == 0) {
            userId = currentSessionUserPrincipal.getId();
        }
        List<TrackUnitUnsubmittedHours> hours;
        if ((currentSessionUserPrincipal.getId() == userId) || currentSessionUserPrincipal.isAdmin()) {
            hours = trackUnitService.getUnsubmittedHours(userId);
        } else {
            Set<Integer> projectIds = getAvailableProjectIds(userId, currentSessionUserPrincipal);
            hours = trackUnitService.getUnsubmittedHours(userId, projectIds);
        }
        return hours.stream()
                .map(unsubmittedHours -> modelMapper.map(unsubmittedHours, TrackUnitUnsubmittedHoursDto.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isUser() or getPrincipal().isAdmin()")
    @PostMapping("/submit")
    public void submitHours(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
                            @Valid @RequestBody TrackUnitSubmitDto submitDto) {
        int userId = submitDto.getUserId() == 0 ? currentSessionUserPrincipal.getId() : submitDto.getUserId();
        if ((currentSessionUserPrincipal.getId() == userId) || currentSessionUserPrincipal.isAdmin()) {
            trackUnitService.submit(userId, submitDto.getWeeks().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } else {
            Set<Integer> projectIds = getAvailableProjectIds(userId, currentSessionUserPrincipal);
            trackUnitService.submit(userId, projectIds, submitDto.getWeeks().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
    }

    private Set<Integer> getAvailableProjectIds(int userId, SessionUserPrincipal currentSessionUserPrincipal) {
        Set<Integer> projectIds = userService.getUserProjectRoles(userId).stream()
                .filter(userProjectRole -> userProjectRole.getProjectRoleId().equals(ProjectRoleEnum.MANAGER)
                        || userProjectRole.getProjectRoleId().equals(ProjectRoleEnum.EMPLOYEE))
                .map(UserProjectRole::getProjectId)
                .collect(Collectors.toSet());
        if (!currentSessionUserPrincipal.isAdmin()) {
            projectIds.retainAll(currentSessionUserPrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER));
            if (projectIds.isEmpty()) {
                throw new ForbiddenException("User has not admin or manager role in projects of requested user;" +
                        " userId = " + userId + ", currentUserId = " + currentSessionUserPrincipal.getId() + ".");
            }
        }
        return projectIds;
    }
}
