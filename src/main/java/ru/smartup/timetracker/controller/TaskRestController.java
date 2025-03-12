package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.core.CurrentSessionEmployeePrincipal;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.dto.ErrorCode;
import ru.smartup.timetracker.dto.project.response.TaskInProjectDto;
import ru.smartup.timetracker.dto.task.request.TaskCreateDto;
import ru.smartup.timetracker.dto.task.response.TaskDto;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.Task;
import ru.smartup.timetracker.exception.ForbiddenException;
import ru.smartup.timetracker.exception.NotProcessedTrackUnitsException;
import ru.smartup.timetracker.exception.NotUniqueDataException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.service.ProjectService;
import ru.smartup.timetracker.service.TaskService;
import ru.smartup.timetracker.service.TrackUnitService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tasks")
public class TaskRestController {
    private final ProjectService projectService;
    private final TaskService taskService;
    private final TrackUnitService trackUnitService;
    private final ModelMapper modelMapper;

    @PreAuthorize("getPrincipal().isEmployeeOrManager(#projectId) or getPrincipal().isAdmin()")
    @GetMapping
    public List<TaskInProjectDto> getTasksFromProject(@RequestParam(value = "projectId") int projectId) {
        Optional<Project> existProject = projectService.getProject(projectId);
        if (existProject.isEmpty()) {
            throw new ResourceNotFoundException("Project was not found by projectId = " + projectId + ".");
        }
        List<Task> tasks = taskService.getTasksFromProject(projectId);
        return tasks.stream()
                .map(task -> modelMapper.map(task, TaskInProjectDto.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isEmployee() or getPrincipal().isAdmin()")
    @GetMapping("/{taskId}")
    public TaskDto getTask(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                           @PathVariable("taskId") long taskId) {
        Optional<Task> existTask = taskService.getTask(taskId);
        if (existTask.isEmpty()) {
            throw new ResourceNotFoundException("Task was not found by taskId = " + taskId + ".");
        }
        Task task = existTask.get();
        if (currentSessionEmployeePrincipal.isNotManagerOrEmployee(task.getProjectId())
                && !currentSessionEmployeePrincipal.isAdmin()) {
            throw new ForbiddenException("Employee has not admin or manager or employee role in project; employeeId = "
                    + currentSessionEmployeePrincipal.getId() + ", projectId = " + task.getProjectId() + ".");
        }
        return modelMapper.map(task, TaskDto.class);
    }

    @PreAuthorize("getPrincipal().isAdmin() or getPrincipal().isManager(#taskCreateDto.getProjectId())")
    @PostMapping
    public void createTask(@Valid @RequestBody TaskCreateDto taskCreateDto) {
        Optional<Project> existProject = projectService.getNotArchivedProject(taskCreateDto.getProjectId());
        if (existProject.isEmpty()) {
            throw new ResourceNotFoundException("Active project was not found by projectId = "
                    + taskCreateDto.getProjectId() + ".");
        }
        if (taskService.isNotUnique(taskCreateDto.getProjectId(), taskCreateDto.getName())) {
            throw new NotUniqueDataException(ErrorCode.NOT_UNIQUE_TASK_NAME, "Task with specified name = '"
                    + taskCreateDto.getName() + "' already exists in project; projectId = "
                    + taskCreateDto.getProjectId() + ".");
        }
        Task task = modelMapper.map(taskCreateDto, Task.class);
        taskService.createTask(task);
    }

    @PreAuthorize("getPrincipal().isAdmin() or getPrincipal().isManager(#taskCreateDto.getProjectId())")
    @PatchMapping("/{taskId}")
    public void updateTask(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                           @Valid @RequestBody TaskCreateDto taskCreateDto,
                           @PathVariable("taskId") long taskId) {
        Optional<Task> existTask = taskService.getNotArchivedTask(taskId);
        if (existTask.isEmpty()) {
            throw new ResourceNotFoundException("Active task was not found by taskId = " + taskId + ".");
        }
        Optional<Project> existProject = projectService.getNotArchivedProject(taskCreateDto.getProjectId());
        if (existProject.isEmpty()) {
            throw new ResourceNotFoundException("Active project was not found by projectId = "
                    + taskCreateDto.getProjectId() + ".");
        }
        Task task = existTask.get();
        if (currentSessionEmployeePrincipal.isNotManager(task.getProjectId())) {
            throw new ForbiddenException("Employee has not manager role in project; employeeId = "
                    + currentSessionEmployeePrincipal.getId() + ", projectId = " + task.getProjectId() + ".");
        }
        if (taskService.isNotUnique(taskCreateDto.getProjectId(), taskId, taskCreateDto.getName())) {
            throw new NotUniqueDataException(ErrorCode.NOT_UNIQUE_TASK_NAME, "Task with specified name = '"
                    + taskCreateDto.getName() + "' already exists in project; projectId = "
                    + taskCreateDto.getProjectId() + ".");
        }
        modelMapper.map(taskCreateDto, task);
        taskService.updateTask(task);
    }

    @PreAuthorize("getPrincipal().isAdmin() or getPrincipal().isManager()")
    @PostMapping("/{taskId}/archive")
    public void archiveTask(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                            @PathVariable("taskId") long taskId) {
        Optional<Task> existTask = taskService.getNotArchivedTask(taskId);
        if (existTask.isEmpty()) {
            throw new ResourceNotFoundException("Active task was not found by taskId = " + taskId + ".");
        }
        Task task = existTask.get();
        if (currentSessionEmployeePrincipal.isNotManager(task.getProjectId())) {
            throw new ForbiddenException("Employee has not manager role in project; employeeId = "
                    + currentSessionEmployeePrincipal.getId() + ", projectId = " + task.getProjectId() + ".");
        }
        if (trackUnitService.hasNoneFinalTrackUnitForTask(taskId)) {
            throw new NotProcessedTrackUnitsException(ErrorCode.NOT_PROCESSED_TRACK_UNITS_FOR_TASK,
                    "Archive is not available now. Please, check all not processed track units of task; taskId = "
                            + taskId + ".");
        }
        taskService.archiveTask(taskId);
    }
}
