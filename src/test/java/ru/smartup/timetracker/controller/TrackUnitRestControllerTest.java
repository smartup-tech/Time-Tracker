package ru.smartup.timetracker.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import ru.smartup.timetracker.core.SessionUserPrincipal;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.project.response.ProjectShortDto;
import ru.smartup.timetracker.dto.tracker.request.TrackUnitCellUpdateDto;
import ru.smartup.timetracker.dto.tracker.request.TrackUnitRowUpdateDto;
import ru.smartup.timetracker.dto.tracker.request.TrackUnitSubmitDto;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitRowDto;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitTableDto;
import ru.smartup.timetracker.dto.user.response.UserShortDto;
import ru.smartup.timetracker.entity.Task;
import ru.smartup.timetracker.entity.TrackUnit;
import ru.smartup.timetracker.entity.field.enumerated.TrackUnitStatusEnum;
import ru.smartup.timetracker.entity.field.sort.UserSortFieldEnum;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.exception.ForbiddenException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.service.*;
import ru.smartup.timetracker.service.freeze.CRUDFreezeService;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TrackUnitRestControllerTest {
    private static final long TASK_ID = 10;
    private static final int USER_ID = 1;
    private static final int USER_ID_QUERY = 2;
    private static final String USER_EMAIL = "user_email";
    private static final int PROJECT_ID_ONE = 1;
    private static final int PROJECT_ID_TWO = 2;
    private static final String PROJECT_NAME = "project_name";
    private static final LocalDate CURRENT_DATE = LocalDate.now();
    private static final LocalDate FIRST_DAY_OF_WEEK = CURRENT_DATE.with(DayOfWeek.MONDAY);
    private static final LocalDate LAST_DAY_OF_WEEK = CURRENT_DATE.with(DayOfWeek.SUNDAY);

    private final TrackUnitService trackUnitService = mock(TrackUnitService.class);
    private final TaskService taskService = mock(TaskService.class);
    private final ProjectService projectService = mock(ProjectService.class);
    private final UserService userService = mock(UserService.class);
    private final ProductionCalendarService productionCalendarService = mock(ProductionCalendarService.class);
    private final ObservationTaskService observationTaskService = mock(ObservationTaskService.class);
    private final CRUDFreezeService CRUDFreezeService = mock(CRUDFreezeService.class);

    private TrackUnitRestController trackUnitRestController;

    @BeforeEach
    public void setUp() {
        trackUnitRestController = new TrackUnitRestController(trackUnitService, taskService, projectService,
                userService, CRUDFreezeService, observationTaskService, productionCalendarService, new WebConfig().modelMapper());
    }

    @Test
    public void getProjects() {
        when(projectService.getProjectsByIds(Set.of(PROJECT_ID_ONE))).thenReturn(List.of(createProject()));
        when(userService.getUserProjectRoles(USER_ID_QUERY))
                .thenReturn(List.of(createUserProjectRole(USER_ID_QUERY, PROJECT_ID_ONE, ProjectRoleEnum.EMPLOYEE)));

        List<ProjectShortDto> projects = trackUnitRestController.getProjects(createSessionUserPrincipal(UserRoleEnum.ROLE_USER,
                ProjectRoleEnum.MANAGER), USER_ID_QUERY);

        verify(projectService, never()).getAllProjects();
        verify(projectService).getProjectsByIds(Set.of(PROJECT_ID_ONE));
        assertEquals(1, projects.size());
        assertEquals(PROJECT_ID_ONE, projects.get(0).getId());
        assertEquals(PROJECT_NAME, projects.get(0).getName());
    }

    @Test
    public void getProjects_shouldReturnForbiddenException() {
        when(projectService.getProjectsByIds(Set.of(PROJECT_ID_ONE))).thenReturn(List.of(createProject()));
        when(userService.getUserProjectRoles(USER_ID_QUERY))
                .thenReturn(List.of(createUserProjectRole(USER_ID_QUERY, PROJECT_ID_TWO, ProjectRoleEnum.EMPLOYEE)));

        assertThrows(ForbiddenException.class, () -> trackUnitRestController.getProjects(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.MANAGER), USER_ID_QUERY));
    }

    @Test
    public void getProjects_whenUserIdNot() {
        when(projectService.getProjectsByIds(Set.of(PROJECT_ID_ONE))).thenReturn(List.of(createProject()));

        List<ProjectShortDto> projects = trackUnitRestController.getProjects(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.EMPLOYEE), USER_ID);

        verify(projectService, never()).getAllProjects();
        verify(projectService).getProjectsByIds(Set.of(PROJECT_ID_ONE));
        assertEquals(1, projects.size());
        assertEquals(PROJECT_ID_ONE, projects.get(0).getId());
        assertEquals(PROJECT_NAME, projects.get(0).getName());
    }

    @Test
    public void getProjects_whenUserIdNotForAdmin() {
        when(projectService.getAllProjects()).thenReturn(List.of(createProject()));

        List<ProjectShortDto> projects = trackUnitRestController.getProjects(createSessionUserPrincipal(
                UserRoleEnum.ROLE_ADMIN, ProjectRoleEnum.EMPLOYEE), USER_ID);

        verify(projectService).getAllProjects();
        verify(projectService, never()).getProjectsByIds(anySet());
        assertEquals(1, projects.size());
        assertEquals(PROJECT_ID_ONE, projects.get(0).getId());
        assertEquals(PROJECT_NAME, projects.get(0).getName());
    }

    @Test
    public void searchUsers() {
        User user = createUser();

        when(userService.searchUsersFromProjects(Set.of(PROJECT_ID_ONE), StringUtils.EMPTY, false,
                Sort.by(Sort.Direction.ASC, UserSortFieldEnum.NAME.getValues())))
                .thenReturn(List.of(user, new User()));

        Collection<UserShortDto> users = trackUnitRestController.searchUsers(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.MANAGER), StringUtils.EMPTY, false);

        verify(userService, never()).getUser(USER_ID);
        assertEquals(2, users.size());
        UserShortDto userShortDto = users.iterator().next();
        assertEquals(USER_ID, userShortDto.getId());
        assertEquals(USER_EMAIL, userShortDto.getEmail());
    }

    @Test
    public void searchUsers_whenAdmin() {
        when(userService.searchUsers(StringUtils.EMPTY, false, Sort.by(Sort.Direction.ASC, UserSortFieldEnum.NAME.getValues())))
                .thenReturn(List.of(createUser(), new User()));

        Collection<UserShortDto> users = trackUnitRestController.searchUsers(createSessionUserPrincipal(
                UserRoleEnum.ROLE_ADMIN, ProjectRoleEnum.EMPLOYEE), StringUtils.EMPTY, false);

        assertEquals(2, users.size());
        UserShortDto user = users.iterator().next();
        assertEquals(USER_ID, user.getId());
        assertEquals(USER_EMAIL, user.getEmail());
    }

    @Test
    public void searchUsers_shouldReturnResourceNotFoundException() {
        when(userService.getUser(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> trackUnitRestController.searchUsers(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.EMPLOYEE), StringUtils.EMPTY, false));
    }

    @Test
    public void searchUsers_whenUserAndEmployee() {
        User user = createUser();

        when(userService.getUser(USER_ID)).thenReturn(Optional.of(user));

        Collection<UserShortDto> users = trackUnitRestController.searchUsers(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.EMPLOYEE), StringUtils.EMPTY, false);

        assertEquals(1, users.size());
        UserShortDto userShortDto = users.iterator().next();
        assertEquals(USER_ID, userShortDto.getId());
        assertEquals(USER_EMAIL, userShortDto.getEmail());
    }

    @Test
    public void getDataForWeek() {
        when(userService.getUserProjectRoles(USER_ID_QUERY))
                .thenReturn(List.of(createUserProjectRole(USER_ID_QUERY, PROJECT_ID_ONE, ProjectRoleEnum.EMPLOYEE)));
        when(trackUnitService.getByUserIdAndProjectIdsAndRange(USER_ID_QUERY, Set.of(PROJECT_ID_ONE), FIRST_DAY_OF_WEEK, LAST_DAY_OF_WEEK))
                .thenReturn(List.of(createTrackUnit()));

        TrackUnitTableDto trackUnitTableDto = trackUnitRestController.getDataForWeek(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.MANAGER), USER_ID_QUERY, CURRENT_DATE);

        assertEquals(1, trackUnitTableDto.getData().size());
        assertEquals(7, trackUnitTableDto.getData().get(0).getUnits().size());
        assertEquals(PROJECT_ID_ONE, trackUnitTableDto.getData().get(0).getProjectId());
        assertEquals(PROJECT_NAME, trackUnitTableDto.getData().get(0).getProjectName());
    }

    @Test
    public void getDataForWeek_shouldReturnEmptyList() {
        TrackUnitTableDto trackUnitTableDto = trackUnitRestController.getDataForWeek(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.EMPLOYEE), USER_ID_QUERY, CURRENT_DATE);

        assertEquals(0, trackUnitTableDto.getData().size());
    }

    @Test
    public void getDataForWeek_whenAdmin() {
        trackUnitRestController.getDataForWeek(createSessionUserPrincipal(
                UserRoleEnum.ROLE_ADMIN, ProjectRoleEnum.EMPLOYEE), USER_ID_QUERY, CURRENT_DATE);

        verify(trackUnitService).getByUserIdAndRange(USER_ID_QUERY, FIRST_DAY_OF_WEEK, LAST_DAY_OF_WEEK);
        verify(CRUDFreezeService).getCacheableLastFreeze();
    }

    @Test
    public void getDataForWeek_whenCurrentUser() {
        trackUnitRestController.getDataForWeek(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.EMPLOYEE), USER_ID, CURRENT_DATE);

        verify(trackUnitService).getByUserIdAndRange(anyInt(), any(), any());
        verify(CRUDFreezeService).getCacheableLastFreeze();
    }

    @Test
    public void updateOrDeleteDataForWeek() {
        TrackUnitRowUpdateDto trackUnitRowUpdateDto = createTrackUnitRowUpdateDto();

        when(taskService.getNotArchivedTask(trackUnitRowUpdateDto.getTaskId())).thenReturn(Optional.of(createTask()));
        when(projectService.getProject(PROJECT_ID_ONE)).thenReturn(Optional.of(createProject()));

        TrackUnitRowDto trackUnitRowDto = trackUnitRestController.updateDataForWeek(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.EMPLOYEE), trackUnitRowUpdateDto);

        verify(trackUnitService, never()).deleteTrackUnits(anyList());
        verify(trackUnitService).insertOrUpdateHoursAndComment(anyList(), any());
        assertEquals(7, trackUnitRowDto.getUnits().size());
        assertEquals(PROJECT_ID_ONE, trackUnitRowDto.getProjectId());
        assertEquals(PROJECT_NAME, trackUnitRowDto.getProjectName());
    }

    @Test
    public void updateOrDeleteDataForWeek_whenDelete() {
        TrackUnitRowUpdateDto trackUnitRowUpdateDto = createTrackUnitRowUpdateDto();

        when(taskService.getNotArchivedTask(trackUnitRowUpdateDto.getTaskId())).thenReturn(Optional.of(createTask()));
        when(projectService.getProject(PROJECT_ID_ONE)).thenReturn(Optional.of(createProject()));

        trackUnitRestController.deleteDataForWeek(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.EMPLOYEE), trackUnitRowUpdateDto);

        verify(trackUnitService).deleteTrackUnits(any());
        verify(trackUnitService, never()).insertOrUpdateHoursAndComment(anyList(), any());
    }

    @Test
    public void updateOrDeleteDataForWeek_shouldReturnResourceNotFoundExceptionForTask() {
        TrackUnitRowUpdateDto trackUnitRowUpdateDto = createTrackUnitRowUpdateDto();

        when(taskService.getNotArchivedTask(trackUnitRowUpdateDto.getTaskId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> trackUnitRestController.updateDataForWeek(any(), trackUnitRowUpdateDto));
    }

    @Test
    public void updateOrDeleteDataForWeek_shouldReturnResourceNotFoundExceptionForProject() {
        TrackUnitRowUpdateDto trackUnitRowUpdateDto = createTrackUnitRowUpdateDto();

        when(taskService.getNotArchivedTask(trackUnitRowUpdateDto.getTaskId())).thenReturn(Optional.of(createTask()));
        when(projectService.getProject(PROJECT_ID_ONE)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> trackUnitRestController.updateDataForWeek(any(), trackUnitRowUpdateDto));
    }

    @Test
    public void updateOrDeleteDataForWeek_shouldReturnForbiddenException() {
        TrackUnitRowUpdateDto trackUnitRowUpdateDto = createTrackUnitRowUpdateDto();

        when(taskService.getNotArchivedTask(trackUnitRowUpdateDto.getTaskId())).thenReturn(Optional.of(createTask()));
        when(projectService.getProject(PROJECT_ID_ONE)).thenReturn(Optional.of(createProject()));

        assertThrows(ForbiddenException.class,
                () -> trackUnitRestController.updateDataForWeek(
                        createSessionUserPrincipal(UserRoleEnum.ROLE_USER, ProjectRoleEnum.EMPLOYEE, PROJECT_ID_TWO),
                        trackUnitRowUpdateDto));
    }

    @Test
    public void getUnsubmittedHours() {
        when(userService.getUserProjectRoles(USER_ID_QUERY))
                .thenReturn(List.of(createUserProjectRole(USER_ID_QUERY, PROJECT_ID_ONE, ProjectRoleEnum.EMPLOYEE)));

        trackUnitRestController.getUnsubmittedHours(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.MANAGER), USER_ID_QUERY);

        verify(trackUnitService).getUnsubmittedHours(USER_ID_QUERY, Set.of(PROJECT_ID_ONE));
    }

    @Test
    public void getUnsubmittedHours_shouldReturnForbiddenException() {
        assertThrows(ForbiddenException.class, () ->
                trackUnitRestController.getUnsubmittedHours(createSessionUserPrincipal(UserRoleEnum.ROLE_USER,
                        ProjectRoleEnum.EMPLOYEE), USER_ID_QUERY));
    }

    @Test
    public void getUnsubmittedHours_whenAdmin() {
        trackUnitRestController.getUnsubmittedHours(createSessionUserPrincipal(
                UserRoleEnum.ROLE_ADMIN, ProjectRoleEnum.EMPLOYEE), USER_ID_QUERY);

        verify(trackUnitService).getUnsubmittedHours(USER_ID_QUERY);
    }

    @Test
    public void getUnsubmittedHours_whenCurrentUser() {
        trackUnitRestController.getUnsubmittedHours(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.EMPLOYEE), USER_ID);

        verify(trackUnitService).getUnsubmittedHours(USER_ID);
    }

    @Test
    public void submitHours() {
        TrackUnitSubmitDto submitDto = createTrackUnitSubmitDto(USER_ID_QUERY);

        when(userService.getUserProjectRoles(USER_ID_QUERY))
                .thenReturn(List.of(createUserProjectRole(USER_ID_QUERY, PROJECT_ID_ONE, ProjectRoleEnum.EMPLOYEE)));

        trackUnitRestController.submitHours(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.MANAGER), submitDto);

        verify(trackUnitService).submit(USER_ID_QUERY, Set.of(PROJECT_ID_ONE), new ArrayList<>(submitDto.getWeeks()));
    }

    @Test
    public void submitHours_shouldReturnForbiddenException() {
        assertThrows(ForbiddenException.class, () ->
                trackUnitRestController.submitHours(createSessionUserPrincipal(
                        UserRoleEnum.ROLE_USER, ProjectRoleEnum.EMPLOYEE), createTrackUnitSubmitDto(USER_ID_QUERY)));
    }

    @Test
    public void submitHours_whenAdmin() {
        TrackUnitSubmitDto submitDto = createTrackUnitSubmitDto(USER_ID_QUERY);

        trackUnitRestController.submitHours(createSessionUserPrincipal(
                UserRoleEnum.ROLE_ADMIN, ProjectRoleEnum.EMPLOYEE), submitDto);

        verify(trackUnitService).submit(USER_ID_QUERY, new ArrayList<>(submitDto.getWeeks()));
    }

    @Test
    public void submitHours_whenCurrentUser() {
        TrackUnitSubmitDto submitDto = createTrackUnitSubmitDto(USER_ID);

        trackUnitRestController.submitHours(createSessionUserPrincipal(
                UserRoleEnum.ROLE_USER, ProjectRoleEnum.EMPLOYEE), submitDto);

        verify(trackUnitService).submit(USER_ID, new ArrayList<>(submitDto.getWeeks()));
    }

    private SessionUserPrincipal createSessionUserPrincipal(UserRoleEnum role, ProjectRoleEnum projectRole) {
        return createSessionUserPrincipal(role, projectRole, PROJECT_ID_ONE);
    }

    private SessionUserPrincipal createSessionUserPrincipal(UserRoleEnum role, ProjectRoleEnum projectRole, int projectId) {
        SessionUserPrincipal sessionUserPrincipal = new SessionUserPrincipal(USER_ID, USER_EMAIL);
        UserRole userRole = new UserRole();
        userRole.setUserId(USER_ID);
        userRole.setRoleId(role);
        UserProjectRole userProjectRole = new UserProjectRole();
        userProjectRole.setProjectId(projectId);
        userProjectRole.setUserId(USER_ID);
        userProjectRole.setProjectRoleId(projectRole);
        sessionUserPrincipal.setAllRoles(List.of(userRole), List.of(userProjectRole));
        return sessionUserPrincipal;
    }

    private Project createProject() {
        Project project = new Project();
        project.setId(PROJECT_ID_ONE);
        project.setName(PROJECT_NAME);
        return project;
    }

    private UserProjectRole createUserProjectRole(int userId, int projectId, ProjectRoleEnum projectRoleEnum) {
        UserProjectRole userProjectRole = new UserProjectRole();
        userProjectRole.setUserId(userId);
        userProjectRole.setProjectId(projectId);
        userProjectRole.setProjectRoleId(projectRoleEnum);
        return userProjectRole;
    }

    private User createUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        return user;
    }

    private TrackUnit createTrackUnit() {
        TrackUnit trackUnit = new TrackUnit();
        trackUnit.setUserId(USER_ID);
        trackUnit.setTask(createTask());
        trackUnit.setProject(createProject());
        trackUnit.setStatus(TrackUnitStatusEnum.CREATED);
        trackUnit.setWorkDay(Date.valueOf(CURRENT_DATE));
        return trackUnit;
    }

    private Task createTask() {
        Task task = new Task();
        task.setId(TASK_ID);
        task.setProjectId(PROJECT_ID_ONE);
        return task;
    }

    private TrackUnitRowUpdateDto createTrackUnitRowUpdateDto() {
        TrackUnitCellUpdateDto trackUnitCellUpdateDto = new TrackUnitCellUpdateDto();
        trackUnitCellUpdateDto.setWorkDay(Date.valueOf(CURRENT_DATE));
        TrackUnitRowUpdateDto trackUnitRowUpdateDto = new TrackUnitRowUpdateDto();
        trackUnitRowUpdateDto.setTaskId(TASK_ID);
        trackUnitRowUpdateDto.setUnits(List.of(trackUnitCellUpdateDto));
        return trackUnitRowUpdateDto;
    }

    private TrackUnitSubmitDto createTrackUnitSubmitDto(int userId) {
        TrackUnitSubmitDto trackUnitSubmitDto = new TrackUnitSubmitDto();
        trackUnitSubmitDto.setWeeks(Set.of(Date.valueOf(CURRENT_DATE)));
        trackUnitSubmitDto.setUserId(userId);
        return trackUnitSubmitDto;
    }
}