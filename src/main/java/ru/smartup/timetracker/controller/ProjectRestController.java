package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.core.CurrentSessionEmployeePrincipal;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.dto.ErrorCode;
import ru.smartup.timetracker.dto.PageableRequestParamDto;
import ru.smartup.timetracker.dto.QueryArchiveParamRequestDto;
import ru.smartup.timetracker.dto.project.request.ProjectCreateDto;
import ru.smartup.timetracker.dto.project.request.EmployeeProjectRoleDeleteDto;
import ru.smartup.timetracker.dto.project.request.EmployeeProjectRoleModifyDto;
import ru.smartup.timetracker.dto.project.response.*;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.field.sort.ProjectSortFieldEnum;
import ru.smartup.timetracker.entity.Task;
import ru.smartup.timetracker.entity.EmployeeProjectRole;
import ru.smartup.timetracker.exception.ForbiddenException;
import ru.smartup.timetracker.exception.NotProcessedTrackUnitsException;
import ru.smartup.timetracker.exception.NotUniqueDataException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.service.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projects")
public class ProjectRestController {
    private final ProjectService projectService;
    private final RelationEmployeeRolesService relationEmployeeRolesService;
    private final TrackUnitService trackUnitService;
    private final EmployeeService employeeService;
    private final TaskService taskService;
    private final ModelMapper modelMapper;
    private final ConversionService conversionService;

    @PreAuthorize("getPrincipal().isAdmin() or getPrincipal().isManager()")
    @GetMapping
    public Page<ProjectShortDto> getProjectsByPage(final @CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                                                   final @Valid QueryArchiveParamRequestDto positionRequestParam,
                                                   final @Valid PageableRequestParamDto<ProjectSortFieldEnum> pageableRequestParam) {
        pageableRequestParam.setSortBy(conversionService.convert(pageableRequestParam.getSortBy(), ProjectSortFieldEnum.class));

        Page<ProjectShortDto> projects;
        if (currentSessionEmployeePrincipal.isAdmin()) {
            projects = projectService.getProjects(positionRequestParam, pageableRequestParam);
        } else {
            Set<Integer> projectIds = currentSessionEmployeePrincipal.getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER);
            if (projectIds.isEmpty()) {
                throw new ForbiddenException("Project ids were not found for employee with manager role; employeeId = "
                        + currentSessionEmployeePrincipal.getId() + ".");
            }
            projects = projectService.getProjectsByIds(projectIds, positionRequestParam, pageableRequestParam);
        }
        return projects;
    }

    /**
     * Получить проект. Пользователь видит только свои проекты, администратор все.
     *
     * @param projectId идентификатор проекта
     * @return ProjectDto
     */
    @PreAuthorize("getPrincipal().isAdmin() or getPrincipal().isManager(#projectId)")
    @GetMapping("/{projectId}")
    public ProjectDetailDto getProject(@PathVariable("projectId") int projectId) {
        Optional<Project> existProject = projectService.getProject(projectId);
        if (existProject.isEmpty()) {
            throw new ResourceNotFoundException("Project was not found by projectId = " + projectId + ".");
        }
        ProjectDetailDto project = modelMapper.map(existProject.get(), ProjectDetailDto.class);
        List<Employee> employees = employeeService.getEmployeesFromProject(projectId);
        project.setEmployees(employees.stream()
                .map(EmployeeInProjectDto::new)
                .collect(Collectors.toList()));
        List<Task> tasks = taskService.getTasksFromProject(projectId);
        project.setTasks(tasks.stream()
                .map(task -> modelMapper.map(task, TaskInProjectDto.class))
                .collect(Collectors.toList()));
        return project;
    }

    /**
     * Создать проект
     *
     * @param projectCreateDto данные проекта
     */
    @PreAuthorize("getPrincipal().isAdmin()")
    @PostMapping
    public void createProject(@Valid @RequestBody ProjectCreateDto projectCreateDto) {
        if (projectService.isNotUnique(projectCreateDto.getName())) {
            throw new NotUniqueDataException(ErrorCode.NOT_UNIQUE_PROJECT_NAME, "Project with specified name = '"
                    + projectCreateDto.getName() + "' already exists.");
        }
        Project project = modelMapper.map(projectCreateDto, Project.class);
        projectService.createProject(project);
    }

    /**
     * Обновить проект (если проект не был переведен в архив)
     *
     * @param projectCreateDto данные проекта
     * @param projectId        идентификатор проекта
     */
    @PreAuthorize("getPrincipal().isAdmin() or getPrincipal().isManager(#projectId)")
    @PatchMapping("/{projectId}")
    public void updateProject(@Valid @RequestBody ProjectCreateDto projectCreateDto,
                              @PathVariable("projectId") int projectId) {
        Optional<Project> existProject = projectService.getNotArchivedProject(projectId);
        if (existProject.isEmpty()) {
            throw new ResourceNotFoundException("Active project was not found by projectId = " + projectId + ".");
        }
        if (projectService.isNotUnique(projectId, projectCreateDto.getName())) {
            throw new NotUniqueDataException(ErrorCode.NOT_UNIQUE_PROJECT_NAME, "Project with specified name = '"
                    + projectCreateDto.getName() + "' already exists.");
        }
        Project project = existProject.get();
        modelMapper.map(projectCreateDto, project);
        projectService.updateProject(project);
    }

    /**
     * Перевести проект в архив
     *
     * @param projectId идентификатор проекта
     */
    @PreAuthorize("getPrincipal().isAdmin()")
    @PostMapping("/{projectId}/archive")
    public void archiveProject(@PathVariable("projectId") int projectId) {
        Optional<Project> existProject = projectService.getNotArchivedProject(projectId);
        if (existProject.isEmpty()) {
            throw new ResourceNotFoundException("Active project was not found by projectId = " + projectId + ".");
        }
        if (trackUnitService.hasNoneFinalTrackUnitForProject(projectId)) {
            throw new NotProcessedTrackUnitsException(ErrorCode.NOT_PROCESSED_TRACK_UNITS_FOR_PROJECT,
                    "Archive is not available now. Please, check all not processed track units of project; projectId = "
                            + projectId + ".");
        }
        projectService.archiveProject(projectId);
    }

    /**
     * Добавить пользователя в проект с указанной ролью или обновить
     *
     * @param employeeProjectRoleModifyDto данные пользователя, добавляемого в проект
     * @param projectId                идентификатор проекта
     */
    @PreAuthorize("getPrincipal().isAdmin() or getPrincipal().isManager(#projectId)")
    @PostMapping("/{projectId}/modifyProjectEmployee")
    public void modifyProjectEmployee(@Valid @RequestBody EmployeeProjectRoleModifyDto employeeProjectRoleModifyDto,
                                      @PathVariable("projectId") int projectId) {
        Optional<Project> existProject = projectService.getNotArchivedProject(projectId);
        if (existProject.isEmpty()) {
            throw new ResourceNotFoundException("Active project was not found by projectId = " + projectId + ".");
        }
        Optional<Employee> existEmployee = employeeService.getNotArchivedEmployee(employeeProjectRoleModifyDto.getEmployeeId());
        if (existEmployee.isEmpty()) {
            throw new ResourceNotFoundException("Active employee was not found by employeeId = "
                    + employeeProjectRoleModifyDto.getEmployeeId() + ".");
        }
        EmployeeProjectRole employeeProjectRole = modelMapper.map(employeeProjectRoleModifyDto, EmployeeProjectRole.class);
        employeeProjectRole.setProjectId(projectId);
        relationEmployeeRolesService.updateEmployeeProjectRole(employeeProjectRole);
    }

    /**
     * Удалить пользователя из проекта
     *
     * @param employeeProjectRoleDeleteDto данные пользователя, удаляемого из проекта
     * @param projectId                идентификатор проекта
     */
    @PreAuthorize("getPrincipal().isAdmin() or getPrincipal().isManager(#projectId)")
    @PostMapping("/{projectId}/deleteProjectEmployee")
    public void deleteProjectEmployee(@Valid @RequestBody EmployeeProjectRoleDeleteDto employeeProjectRoleDeleteDto,
                                      @PathVariable("projectId") int projectId) {
        Optional<Project> existProject = projectService.getNotArchivedProject(projectId);
        if (existProject.isEmpty()) {
            throw new ResourceNotFoundException("Active project was not found by projectId = " + projectId + ".");
        }
        Optional<Employee> existEmployee = employeeService.getNotArchivedEmployee(employeeProjectRoleDeleteDto.getEmployeeId());
        if (existEmployee.isEmpty()) {
            throw new ResourceNotFoundException("Active employee was not found by employeeId = "
                    + employeeProjectRoleDeleteDto.getEmployeeId() + ".");
        }
        relationEmployeeRolesService.deleteEmployeeProjectRole(employeeProjectRoleDeleteDto.getEmployeeId(), projectId);
    }

    @PreAuthorize("getPrincipal().isEmployee() or getPrincipal().isAdmin()")
    @GetMapping("/employee")
    public List<ProjectOfEmployeeDto> getProjectsOfEmployee(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                                                            @RequestParam(defaultValue = "0") int employeeId) {
        if (employeeId <= 0) {
            employeeId = currentSessionEmployeePrincipal.getId();
        }
        if ((currentSessionEmployeePrincipal.getId() != employeeId) && !currentSessionEmployeePrincipal.isAdmin()) {
            throw new ForbiddenException("Employee is not admin and is not a selected employee; employeeId = "
                    + employeeId + ", current employeeId = " + currentSessionEmployeePrincipal.getId() + ".");
        }
        return projectService.getNotArchivedProjectsOfEmployeeWithRole(employeeId).stream()
                .map(project -> modelMapper.map(project, ProjectOfEmployeeDto.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @GetMapping("/active")
    public List<ProjectShortDto> getActiveProjects() {
        return projectService.getActiveProjects().stream()
                .map(project -> modelMapper.map(project, ProjectShortDto.class)).collect(Collectors.toList());
    }
}
