package ru.smartup.timetracker.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.employee.response.EmployeeShortDto;
import ru.smartup.timetracker.dto.project.response.ProjectShortDto;
import ru.smartup.timetracker.dto.tracker.request.TrackUnitCellUpdateDto;
import ru.smartup.timetracker.dto.tracker.request.TrackUnitRowUpdateDto;
import ru.smartup.timetracker.dto.tracker.request.TrackUnitSubmitDto;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitRowDto;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitTableDto;
import ru.smartup.timetracker.entity.*;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.TrackUnitStatusEnum;
import ru.smartup.timetracker.entity.field.sort.EmployeeSortFieldEnum;
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
    private static final int EMPLOYEE_ID = 1;
    private static final int EMPLOYEE_ID_QUERY = 2;
    private static final String EMPLOYEE_EMAIL = "employee_email";
    private static final int PROJECT_ID_ONE = 1;
    private static final int PROJECT_ID_TWO = 2;
    private static final String PROJECT_NAME = "project_name";
    private static final LocalDate CURRENT_DATE = LocalDate.now();
    private static final LocalDate FIRST_DAY_OF_WEEK = CURRENT_DATE.with(DayOfWeek.MONDAY);
    private static final LocalDate LAST_DAY_OF_WEEK = CURRENT_DATE.with(DayOfWeek.SUNDAY);

    private final TrackUnitService trackUnitService = mock(TrackUnitService.class);
    private final TaskService taskService = mock(TaskService.class);
    private final ProjectService projectService = mock(ProjectService.class);
    private final EmployeeService employeeService = mock(EmployeeService.class);
    private final ProductionCalendarService productionCalendarService = mock(ProductionCalendarService.class);
    private final ObservationTaskService observationTaskService = mock(ObservationTaskService.class);
    private final CRUDFreezeService CRUDFreezeService = mock(CRUDFreezeService.class);

    private TrackUnitRestController trackUnitRestController;

    @BeforeEach
    public void setUp() {
        trackUnitRestController = new TrackUnitRestController(trackUnitService, taskService, projectService,
                employeeService, CRUDFreezeService, observationTaskService, productionCalendarService, new WebConfig().modelMapper());
    }

    @Test
    public void getProjects() {
        when(projectService.getProjectsByIds(Set.of(PROJECT_ID_ONE))).thenReturn(List.of(createProject()));
        when(employeeService.getEmployeeProjectRoles(EMPLOYEE_ID_QUERY))
                .thenReturn(List.of(createEmployeeProjectRole(EMPLOYEE_ID_QUERY, PROJECT_ID_ONE, ProjectRoleEnum.EMPLOYEE)));

        List<ProjectShortDto> projects = trackUnitRestController.getProjects(createSessionEmployeePrincipal(EmployeeRoleEnum.ROLE_EMPLOYEE,
                ProjectRoleEnum.MANAGER), EMPLOYEE_ID_QUERY);

        verify(projectService, never()).getAllProjects();
        verify(projectService).getProjectsByIds(Set.of(PROJECT_ID_ONE));
        assertEquals(1, projects.size());
        assertEquals(PROJECT_ID_ONE, projects.get(0).getId());
        assertEquals(PROJECT_NAME, projects.get(0).getName());
    }

    @Test
    public void getProjects_shouldReturnForbiddenException() {
        when(projectService.getProjectsByIds(Set.of(PROJECT_ID_ONE))).thenReturn(List.of(createProject()));
        when(employeeService.getEmployeeProjectRoles(EMPLOYEE_ID_QUERY))
                .thenReturn(List.of(createEmployeeProjectRole(EMPLOYEE_ID_QUERY, PROJECT_ID_TWO, ProjectRoleEnum.EMPLOYEE)));

        assertThrows(ForbiddenException.class, () -> trackUnitRestController.getProjects(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.MANAGER), EMPLOYEE_ID_QUERY));
    }

    @Test
    public void getProjects_whenEmployeeIdNot() {
        when(projectService.getProjectsByIds(Set.of(PROJECT_ID_ONE))).thenReturn(List.of(createProject()));

        List<ProjectShortDto> projects = trackUnitRestController.getProjects(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.EMPLOYEE), EMPLOYEE_ID);

        verify(projectService, never()).getAllProjects();
        verify(projectService).getProjectsByIds(Set.of(PROJECT_ID_ONE));
        assertEquals(1, projects.size());
        assertEquals(PROJECT_ID_ONE, projects.get(0).getId());
        assertEquals(PROJECT_NAME, projects.get(0).getName());
    }

    @Test
    public void getProjects_whenEmployeeIdNotForAdmin() {
        when(projectService.getAllProjects()).thenReturn(List.of(createProject()));

        List<ProjectShortDto> projects = trackUnitRestController.getProjects(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_ADMIN, ProjectRoleEnum.EMPLOYEE), EMPLOYEE_ID);

        verify(projectService).getAllProjects();
        verify(projectService, never()).getProjectsByIds(anySet());
        assertEquals(1, projects.size());
        assertEquals(PROJECT_ID_ONE, projects.get(0).getId());
        assertEquals(PROJECT_NAME, projects.get(0).getName());
    }

    @Test
    public void searchEmployees() {
        Employee employee = createEmployee();

        when(employeeService.searchEmployeesFromProjects(Set.of(PROJECT_ID_ONE), StringUtils.EMPTY, false,
                Sort.by(Sort.Direction.ASC, EmployeeSortFieldEnum.NAME.getValues())))
                .thenReturn(List.of(employee, new Employee()));

        Collection<EmployeeShortDto> employees = trackUnitRestController.searchEmployees(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.MANAGER), StringUtils.EMPTY, false);

        verify(employeeService, never()).getEmployee(EMPLOYEE_ID);
        assertEquals(2, employees.size());
        EmployeeShortDto employeeShortDto = employees.iterator().next();
        assertEquals(EMPLOYEE_ID, employeeShortDto.getId());
        assertEquals(EMPLOYEE_EMAIL, employeeShortDto.getEmail());
    }

    @Test
    public void searchEmployees_whenAdmin() {
        when(employeeService.searchEmployees(StringUtils.EMPTY, false, Sort.by(Sort.Direction.ASC, EmployeeSortFieldEnum.NAME.getValues())))
                .thenReturn(List.of(createEmployee(), new Employee()));

        Collection<EmployeeShortDto> employees = trackUnitRestController.searchEmployees(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_ADMIN, ProjectRoleEnum.EMPLOYEE), StringUtils.EMPTY, false);

        assertEquals(2, employees.size());
        EmployeeShortDto employee = employees.iterator().next();
        assertEquals(EMPLOYEE_ID, employee.getId());
        assertEquals(EMPLOYEE_EMAIL, employee.getEmail());
    }

    @Test
    public void searchEmployees_shouldReturnResourceNotFoundException() {
        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> trackUnitRestController.searchEmployees(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.EMPLOYEE), StringUtils.EMPTY, false));
    }

    @Test
    public void searchEmployees_whenEmployeeAndEmployee() {
        Employee employee = createEmployee();

        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

        Collection<EmployeeShortDto> employees = trackUnitRestController.searchEmployees(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.EMPLOYEE), StringUtils.EMPTY, false);

        assertEquals(1, employees.size());
        EmployeeShortDto employeeShortDto = employees.iterator().next();
        assertEquals(EMPLOYEE_ID, employeeShortDto.getId());
        assertEquals(EMPLOYEE_EMAIL, employeeShortDto.getEmail());
    }

    @Test
    public void getDataForWeek() {
        when(employeeService.getEmployeeProjectRoles(EMPLOYEE_ID_QUERY))
                .thenReturn(List.of(createEmployeeProjectRole(EMPLOYEE_ID_QUERY, PROJECT_ID_ONE, ProjectRoleEnum.EMPLOYEE)));
        when(trackUnitService.getByEmployeeIdAndProjectIdsAndRange(EMPLOYEE_ID_QUERY, Set.of(PROJECT_ID_ONE), FIRST_DAY_OF_WEEK, LAST_DAY_OF_WEEK))
                .thenReturn(List.of(createTrackUnit()));

        TrackUnitTableDto trackUnitTableDto = trackUnitRestController.getDataForWeek(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.MANAGER), EMPLOYEE_ID_QUERY, CURRENT_DATE);

        assertEquals(1, trackUnitTableDto.getData().size());
        assertEquals(7, trackUnitTableDto.getData().get(0).getUnits().size());
        assertEquals(PROJECT_ID_ONE, trackUnitTableDto.getData().get(0).getProjectId());
        assertEquals(PROJECT_NAME, trackUnitTableDto.getData().get(0).getProjectName());
    }

    @Test
    public void getDataForWeek_shouldReturnEmptyList() {
        TrackUnitTableDto trackUnitTableDto = trackUnitRestController.getDataForWeek(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.EMPLOYEE), EMPLOYEE_ID_QUERY, CURRENT_DATE);

        assertEquals(0, trackUnitTableDto.getData().size());
    }

    @Test
    public void getDataForWeek_whenAdmin() {
        trackUnitRestController.getDataForWeek(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_ADMIN, ProjectRoleEnum.EMPLOYEE), EMPLOYEE_ID_QUERY, CURRENT_DATE);

        verify(trackUnitService).getByEmployeeIdAndRange(EMPLOYEE_ID_QUERY, FIRST_DAY_OF_WEEK, LAST_DAY_OF_WEEK);
        verify(CRUDFreezeService).getCacheableLastFreeze();
    }

    @Test
    public void getDataForWeek_whenCurrentEmployee() {
        trackUnitRestController.getDataForWeek(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.EMPLOYEE), EMPLOYEE_ID, CURRENT_DATE);

        verify(trackUnitService).getByEmployeeIdAndRange(anyInt(), any(), any());
        verify(CRUDFreezeService).getCacheableLastFreeze();
    }

    @Test
    public void updateOrDeleteDataForWeek() {
        TrackUnitRowUpdateDto trackUnitRowUpdateDto = createTrackUnitRowUpdateDto();

        when(taskService.getNotArchivedTask(trackUnitRowUpdateDto.getTaskId())).thenReturn(Optional.of(createTask()));
        when(projectService.getProject(PROJECT_ID_ONE)).thenReturn(Optional.of(createProject()));

        TrackUnitRowDto trackUnitRowDto = trackUnitRestController.updateDataForWeek(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.EMPLOYEE), trackUnitRowUpdateDto);

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

        trackUnitRestController.deleteDataForWeek(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.EMPLOYEE), trackUnitRowUpdateDto);

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
                        createSessionEmployeePrincipal(EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.EMPLOYEE, PROJECT_ID_TWO),
                        trackUnitRowUpdateDto));
    }

    @Test
    public void getUnsubmittedHours() {
        when(employeeService.getEmployeeProjectRoles(EMPLOYEE_ID_QUERY))
                .thenReturn(List.of(createEmployeeProjectRole(EMPLOYEE_ID_QUERY, PROJECT_ID_ONE, ProjectRoleEnum.EMPLOYEE)));

        trackUnitRestController.getUnsubmittedHours(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.MANAGER), EMPLOYEE_ID_QUERY);

        verify(trackUnitService).getUnsubmittedHours(EMPLOYEE_ID_QUERY, Set.of(PROJECT_ID_ONE));
    }

    @Test
    public void getUnsubmittedHours_shouldReturnForbiddenException() {
        assertThrows(ForbiddenException.class, () ->
                trackUnitRestController.getUnsubmittedHours(createSessionEmployeePrincipal(EmployeeRoleEnum.ROLE_EMPLOYEE,
                        ProjectRoleEnum.EMPLOYEE), EMPLOYEE_ID_QUERY));
    }

    @Test
    public void getUnsubmittedHours_whenAdmin() {
        trackUnitRestController.getUnsubmittedHours(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_ADMIN, ProjectRoleEnum.EMPLOYEE), EMPLOYEE_ID_QUERY);

        verify(trackUnitService).getUnsubmittedHours(EMPLOYEE_ID_QUERY);
    }

    @Test
    public void getUnsubmittedHours_whenCurrentEmployee() {
        trackUnitRestController.getUnsubmittedHours(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.EMPLOYEE), EMPLOYEE_ID);

        verify(trackUnitService).getUnsubmittedHours(EMPLOYEE_ID);
    }

    @Test
    public void submitHours() {
        TrackUnitSubmitDto submitDto = createTrackUnitSubmitDto(EMPLOYEE_ID_QUERY);

        when(employeeService.getEmployeeProjectRoles(EMPLOYEE_ID_QUERY))
                .thenReturn(List.of(createEmployeeProjectRole(EMPLOYEE_ID_QUERY, PROJECT_ID_ONE, ProjectRoleEnum.EMPLOYEE)));

        trackUnitRestController.submitHours(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.MANAGER), submitDto);

        verify(trackUnitService).submit(EMPLOYEE_ID_QUERY, Set.of(PROJECT_ID_ONE), new ArrayList<>(submitDto.getWeeks()));
    }

    @Test
    public void submitHours_shouldReturnForbiddenException() {
        assertThrows(ForbiddenException.class, () ->
                trackUnitRestController.submitHours(createSessionEmployeePrincipal(
                        EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.EMPLOYEE), createTrackUnitSubmitDto(EMPLOYEE_ID_QUERY)));
    }

    @Test
    public void submitHours_whenAdmin() {
        TrackUnitSubmitDto submitDto = createTrackUnitSubmitDto(EMPLOYEE_ID_QUERY);

        trackUnitRestController.submitHours(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_ADMIN, ProjectRoleEnum.EMPLOYEE), submitDto);

        verify(trackUnitService).submit(EMPLOYEE_ID_QUERY, new ArrayList<>(submitDto.getWeeks()));
    }

    @Test
    public void submitHours_whenCurrentEmployee() {
        TrackUnitSubmitDto submitDto = createTrackUnitSubmitDto(EMPLOYEE_ID);

        trackUnitRestController.submitHours(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.EMPLOYEE), submitDto);

        verify(trackUnitService).submit(EMPLOYEE_ID, new ArrayList<>(submitDto.getWeeks()));
    }

    private SessionEmployeePrincipal createSessionEmployeePrincipal(EmployeeRoleEnum role, ProjectRoleEnum projectRole) {
        return createSessionEmployeePrincipal(role, projectRole, PROJECT_ID_ONE);
    }

    private SessionEmployeePrincipal createSessionEmployeePrincipal(EmployeeRoleEnum role, ProjectRoleEnum projectRole, int projectId) {
        SessionEmployeePrincipal sessionEmployeePrincipal = new SessionEmployeePrincipal(EMPLOYEE_ID, EMPLOYEE_EMAIL);
        EmployeeRole employeeRole = new EmployeeRole();
        employeeRole.setEmployeeId(EMPLOYEE_ID);
        employeeRole.setRoleId(role);
        EmployeeProjectRole employeeProjectRole = new EmployeeProjectRole();
        employeeProjectRole.setProjectId(projectId);
        employeeProjectRole.setEmployeeId(EMPLOYEE_ID);
        employeeProjectRole.setProjectRoleId(projectRole);
        sessionEmployeePrincipal.setAllRoles(List.of(employeeRole), List.of(employeeProjectRole));
        return sessionEmployeePrincipal;
    }

    private Project createProject() {
        Project project = new Project();
        project.setId(PROJECT_ID_ONE);
        project.setName(PROJECT_NAME);
        return project;
    }

    private EmployeeProjectRole createEmployeeProjectRole(int employeeId, int projectId, ProjectRoleEnum projectRoleEnum) {
        EmployeeProjectRole employeeProjectRole = new EmployeeProjectRole();
        employeeProjectRole.setEmployeeId(employeeId);
        employeeProjectRole.setProjectId(projectId);
        employeeProjectRole.setProjectRoleId(projectRoleEnum);
        return employeeProjectRole;
    }

    private Employee createEmployee() {
        Employee employee = new Employee();
        employee.setId(EMPLOYEE_ID);
        employee.setEmail(EMPLOYEE_EMAIL);
        return employee;
    }

    private TrackUnit createTrackUnit() {
        TrackUnit trackUnit = new TrackUnit();
        trackUnit.setEmployeeId(EMPLOYEE_ID);
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

    private TrackUnitSubmitDto createTrackUnitSubmitDto(int employeeId) {
        TrackUnitSubmitDto trackUnitSubmitDto = new TrackUnitSubmitDto();
        trackUnitSubmitDto.setWeeks(Set.of(Date.valueOf(CURRENT_DATE)));
        trackUnitSubmitDto.setEmployeeId(employeeId);
        return trackUnitSubmitDto;
    }
}