package ru.smartup.timetracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.project.response.TaskInProjectDto;
import ru.smartup.timetracker.dto.task.request.TaskCreateDto;
import ru.smartup.timetracker.dto.task.response.TaskDto;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.Task;
import ru.smartup.timetracker.entity.EmployeeProjectRole;
import ru.smartup.timetracker.entity.EmployeeRole;
import ru.smartup.timetracker.exception.ForbiddenException;
import ru.smartup.timetracker.exception.NotProcessedTrackUnitsException;
import ru.smartup.timetracker.exception.NotUniqueDataException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.service.ProjectService;
import ru.smartup.timetracker.service.TaskService;
import ru.smartup.timetracker.service.TrackUnitService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class TaskRestControllerTest {
    private static final int EMPLOYEE_ID = 1;
    private static final String EMPLOYEE_EMAIL = "employee_email";
    private static final int PROJECT_ID = 1;
    private static final long TASK_ID = 2;
    private static final String PROJECT_NAME = "project_name";
    private static final String TASK_NAME = "task_name";

    private final ProjectService projectService = mock(ProjectService.class);
    private final TaskService taskService = mock(TaskService.class);
    private final TrackUnitService trackUnitService = mock(TrackUnitService.class);
    private TaskRestController taskRestController;

    @BeforeEach
    public void setUp() {
        taskRestController = new TaskRestController(projectService, taskService, trackUnitService, new WebConfig().modelMapper());
    }

    @Test
    public void getTasksFromProject() {
        when(projectService.getProject(PROJECT_ID)).thenReturn(Optional.of(createProject()));
        when(taskService.getTasksFromProject(PROJECT_ID)).thenReturn(List.of(new Task()));

        List<TaskInProjectDto> tasks = taskRestController.getTasksFromProject(PROJECT_ID);

        verify(taskService).getTasksFromProject(PROJECT_ID);
        assertEquals(1, tasks.size());
    }

    @Test
    public void getTasksFromProject_shouldReturnException() {
        when(projectService.getProject(PROJECT_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskRestController.getTasksFromProject(PROJECT_ID));

    }

    @Test
    public void getTask() {
        when(taskService.getTask(TASK_ID)).thenReturn(Optional.of(createTaskObj()));

        TaskDto taskDto = taskRestController.getTask(createSessionEmployeePrincipal(EmployeeRoleEnum.ROLE_EMPLOYEE,
                ProjectRoleEnum.EMPLOYEE), TASK_ID);

        assertEquals(TASK_ID, taskDto.getId());
        assertEquals(TASK_NAME, taskDto.getName());
    }

    @Test
    public void getTask_shouldReturnForbiddenException() {
        Task task = createTaskObj();
        task.setProjectId(PROJECT_ID);

        when(taskService.getTask(TASK_ID)).thenReturn(Optional.of(task));

        assertThrows(ForbiddenException.class, () -> taskRestController.getTask(
                createSessionEmployeePrincipal(EmployeeRoleEnum.ROLE_EMPLOYEE,
                        ProjectRoleEnum.EMPLOYEE), TASK_ID));
    }

    @Test
    public void getTask_shouldReturnResourceNotFoundException() {
        when(taskService.getTask(TASK_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskRestController.getTask(null, TASK_ID));
    }

    @Test
    public void createTask() {
        TaskCreateDto taskCreateDto = new TaskCreateDto();
        taskCreateDto.setProjectId(PROJECT_ID);
        taskCreateDto.setName(TASK_NAME);

        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(createProject()));
        when(taskService.isNotUnique(PROJECT_ID, TASK_NAME)).thenReturn(false);

        taskRestController.createTask(taskCreateDto);

        verify(taskService).createTask(argThat(task -> task.getName().equals(TASK_NAME)));
    }

    @Test
    public void createTask_shouldReturnResourceNotFoundException() {
        TaskCreateDto taskCreateDto = new TaskCreateDto();
        taskCreateDto.setProjectId(PROJECT_ID);

        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskRestController.createTask(taskCreateDto));
    }

    @Test
    public void createTask_shouldReturnNotUniqueDataException() {
        TaskCreateDto taskCreateDto = new TaskCreateDto();
        taskCreateDto.setProjectId(PROJECT_ID);
        taskCreateDto.setName(TASK_NAME);

        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(createProject()));
        when(taskService.isNotUnique(PROJECT_ID, TASK_NAME)).thenReturn(true);

        assertThrows(NotUniqueDataException.class, () -> taskRestController.createTask(taskCreateDto));
    }

    @Test
    public void updateTask() {
        TaskCreateDto taskCreateDto = new TaskCreateDto();
        taskCreateDto.setProjectId(PROJECT_ID);
        taskCreateDto.setName(TASK_NAME);
        Task task = createTaskObj();
        task.setProjectId(PROJECT_ID);

        when(taskService.getNotArchivedTask(TASK_ID)).thenReturn(Optional.of(task));
        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(createProject()));
        when(taskService.isNotUnique(PROJECT_ID, TASK_ID, TASK_NAME)).thenReturn(false);

        taskRestController.updateTask(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.MANAGER, PROJECT_ID), taskCreateDto, TASK_ID);

        verify(taskService).updateTask(task);
    }

    @Test
    public void updateTask_shouldReturnResourceNotFoundExceptionForTask() {
        when(taskService.getNotArchivedTask(TASK_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                taskRestController.updateTask(null, null, TASK_ID));
    }

    @Test
    public void updateTask_shouldReturnResourceNotFoundExceptionForProject() {
        TaskCreateDto taskCreateDto = new TaskCreateDto();

        when(taskService.getNotArchivedTask(TASK_ID)).thenReturn(Optional.of(createTaskObj()));
        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                taskRestController.updateTask(null, taskCreateDto, TASK_ID));
    }

    @Test
    public void updateTask_shouldReturnForbiddenException() {
        TaskCreateDto taskCreateDto = new TaskCreateDto();
        taskCreateDto.setProjectId(PROJECT_ID);

        when(taskService.getNotArchivedTask(TASK_ID)).thenReturn(Optional.of(createTaskObj()));
        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(createProject()));

        assertThrows(ForbiddenException.class, () -> taskRestController.updateTask(
                createSessionEmployeePrincipal(EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.EMPLOYEE), taskCreateDto, TASK_ID));
    }

    @Test
    public void updateTask_shouldReturnNotUniqueDataException() {
        TaskCreateDto taskCreateDto = new TaskCreateDto();
        taskCreateDto.setProjectId(PROJECT_ID);
        taskCreateDto.setName(TASK_NAME);
        Task task = createTaskObj();
        task.setProjectId(PROJECT_ID);

        when(taskService.getNotArchivedTask(TASK_ID)).thenReturn(Optional.of(task));
        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(createProject()));
        when(taskService.isNotUnique(PROJECT_ID, TASK_ID, TASK_NAME)).thenReturn(true);

        assertThrows(NotUniqueDataException.class, () -> taskRestController.updateTask(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.MANAGER, PROJECT_ID), taskCreateDto, TASK_ID));
    }

    @Test
    public void archiveTask() {
        Task task = createTaskObj();
        task.setProjectId(PROJECT_ID);

        when(taskService.getNotArchivedTask(TASK_ID)).thenReturn(Optional.of(task));

        taskRestController.archiveTask(createSessionEmployeePrincipal(
                EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.MANAGER, PROJECT_ID), TASK_ID);

        verify(taskService).archiveTask(TASK_ID);
    }

    @Test
    public void archiveTask_shouldReturnResourceNotFoundException() {
        when(taskService.getNotArchivedTask(TASK_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                taskRestController.archiveTask(null, TASK_ID));
    }

    @Test
    public void archiveTask_shouldReturnForbiddenException() {
        Task task = createTaskObj();
        task.setProjectId(PROJECT_ID);

        when(taskService.getNotArchivedTask(TASK_ID)).thenReturn(Optional.of(task));

        assertThrows(ForbiddenException.class, () ->
                taskRestController.archiveTask(createSessionEmployeePrincipal(
                        EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.EMPLOYEE, PROJECT_ID), TASK_ID));
    }

    @Test
    public void archiveTask_shouldReturnNotProcessedTrackUnitsException() {
        Task task = createTaskObj();
        task.setProjectId(PROJECT_ID);

        when(taskService.getNotArchivedTask(TASK_ID)).thenReturn(Optional.of(task));
        when(trackUnitService.hasNoneFinalTrackUnitForTask(TASK_ID)).thenReturn(true);

        assertThrows(NotProcessedTrackUnitsException.class, () ->
                taskRestController.archiveTask(createSessionEmployeePrincipal(
                        EmployeeRoleEnum.ROLE_EMPLOYEE, ProjectRoleEnum.MANAGER, PROJECT_ID), TASK_ID));
    }

    private Project createProject() {
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setName(PROJECT_NAME);
        return project;
    }

    private Task createTaskObj() {
        Task task = new Task();
        task.setId(TASK_ID);
        task.setName(TASK_NAME);
        return task;
    }

    private SessionEmployeePrincipal createSessionEmployeePrincipal(EmployeeRoleEnum role, ProjectRoleEnum projectRole) {
        return createSessionEmployeePrincipal(role, projectRole, 0);
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
}