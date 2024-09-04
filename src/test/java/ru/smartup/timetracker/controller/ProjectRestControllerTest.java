package ru.smartup.timetracker.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import ru.smartup.timetracker.core.SessionUserPrincipal;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.PageableRequestParamDto;
import ru.smartup.timetracker.dto.QueryArchiveParamRequestDto;
import ru.smartup.timetracker.dto.project.request.ProjectCreateDto;
import ru.smartup.timetracker.dto.project.request.UserProjectRoleDeleteDto;
import ru.smartup.timetracker.dto.project.request.UserProjectRoleModifyDto;
import ru.smartup.timetracker.dto.project.response.ProjectDetailDto;
import ru.smartup.timetracker.dto.project.response.ProjectShortDto;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.field.sort.ProjectSortFieldEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.entity.field.sort.ProjectSortFieldEnum;
import ru.smartup.timetracker.exception.ForbiddenException;
import ru.smartup.timetracker.exception.NotProcessedTrackUnitsException;
import ru.smartup.timetracker.exception.NotUniqueDataException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.service.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ProjectRestControllerTest {
    private static final int PAGE = 1;
    private static final int SIZE = 10;
    private static final int USER_ID_ONE = 1;
    private static final int USER_ID_TWO = 2;
    private static final String USER_EMAIL = "user_email";
    private static final int PROJECT_ID = 1;
    private static final String PROJECT_NAME = "project_name";

    private final ProjectService projectService = mock(ProjectService.class);
    private final RelationUserRolesService relationUserRolesService = mock(RelationUserRolesService.class);
    private final TrackUnitService trackUnitService = mock(TrackUnitService.class);
    private final UserService userService = mock(UserService.class);
    private final TaskService taskService = mock(TaskService.class);
    private final ConversionService conversionService = mock(ConversionService.class);
    private ModelMapper modelMapper;

    private ProjectRestController projectRestController;

    @BeforeEach
    public void setUp() {
        modelMapper = new WebConfig().modelMapper();
        projectRestController = new ProjectRestController(projectService, relationUserRolesService,
                trackUnitService, userService, taskService, modelMapper, conversionService);
    }

    @Test
    public void getProjectsByPage_whenAdmin() {
        Page<ProjectShortDto> projects = new PageImpl<>(Stream.of(createProjectObj()).map(project -> modelMapper.map(project, ProjectShortDto.class)).collect(Collectors.toList()));

        QueryArchiveParamRequestDto projectParam = createProjectParam("", false);
        PageableRequestParamDto pageableParam = createPageableParam(PAGE, SIZE, ProjectSortFieldEnum.NAME, Sort.Direction.ASC);

        when(projectService.getProjects(projectParam, pageableParam)).thenReturn(projects);

        Page<ProjectShortDto> projectsByPage = projectRestController
                .getProjectsByPage(createSessionUserPrincipal(UserRoleEnum.ROLE_ADMIN, ProjectRoleEnum.MANAGER),
                        projectParam, pageableParam);

        assertEquals(1, projectsByPage.getTotalElements());
    }

    @Test
    public void getProjectsByPage_whenUser() {
        Page<ProjectShortDto> projects = new PageImpl<>(Stream.of(createProjectObj()).map(project -> modelMapper.map(project, ProjectShortDto.class)).collect(Collectors.toList()));

        QueryArchiveParamRequestDto projectParam = createProjectParam("", false);
        PageableRequestParamDto pageableParam = createPageableParam(PAGE, SIZE, ProjectSortFieldEnum.NAME, Sort.Direction.ASC);

        when(projectService.getProjectsByIds(Set.of(PROJECT_ID), projectParam, pageableParam)).thenReturn(projects);

        Page<ProjectShortDto> projectsByPage = projectRestController
                .getProjectsByPage(createSessionUserPrincipal(UserRoleEnum.ROLE_USER, ProjectRoleEnum.MANAGER),
                        projectParam, pageableParam);

        assertEquals(1, projectsByPage.getTotalElements());
    }

    @Test
    public void getProjectsByPage_whenAdminAndSearchQuery() {
        Page<ProjectShortDto> projects = new PageImpl<>(Stream.of(createProjectObj()).map(project -> modelMapper.map(project, ProjectShortDto.class)).collect(Collectors.toList()));

        QueryArchiveParamRequestDto projectParam = createProjectParam(PROJECT_NAME, false);
        PageableRequestParamDto pageableParam = createPageableParam(PAGE, SIZE, ProjectSortFieldEnum.NAME, Sort.Direction.ASC);

        when(projectService.getProjects(projectParam, pageableParam)).thenReturn(projects);

        Page<ProjectShortDto> projectsByPage = projectRestController
                .getProjectsByPage(createSessionUserPrincipal(UserRoleEnum.ROLE_ADMIN, ProjectRoleEnum.MANAGER),
                        projectParam, pageableParam);

        assertEquals(1, projectsByPage.getTotalElements());
    }

    @Test
    public void getProjectsByPage_whenUserAndSearchQuery() {
        Page<ProjectShortDto> projects = new PageImpl<>(Stream.of(createProjectObj()).map(project -> modelMapper.map(project, ProjectShortDto.class)).collect(Collectors.toList()));

        QueryArchiveParamRequestDto projectParam = createProjectParam(PROJECT_NAME, false);
        PageableRequestParamDto pageableParam = createPageableParam(PAGE, SIZE, ProjectSortFieldEnum.NAME, Sort.Direction.ASC);

        when(projectService.getProjectsByIds(Set.of(PROJECT_ID), projectParam, pageableParam)).thenReturn(projects);

        Page<ProjectShortDto> projectsByPage = projectRestController
                .getProjectsByPage(createSessionUserPrincipal(UserRoleEnum.ROLE_USER, ProjectRoleEnum.MANAGER),
                        projectParam, pageableParam);

        assertEquals(1, projectsByPage.getTotalElements());
    }

    @Test
    public void getProject() {
        when(projectService.getProject(PROJECT_ID)).thenReturn(Optional.of(createProjectObj()));

        ProjectDetailDto projectDetailDto = projectRestController.getProject(PROJECT_ID);

        verify(userService).getUsersFromProject(PROJECT_ID);
        verify(taskService).getTasksFromProject(PROJECT_ID);
        assertEquals(PROJECT_ID, projectDetailDto.getId());
    }

    @Test
    public void getProject_shouldReturnException() {
        when(projectService.getProject(PROJECT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectRestController.getProject(PROJECT_ID));
    }

    @Test
    public void createProject() {
        ProjectCreateDto projectCreateDto = new ProjectCreateDto();
        projectCreateDto.setName(PROJECT_NAME);

        when(projectService.isNotUnique(PROJECT_NAME)).thenReturn(false);

        projectRestController.createProject(projectCreateDto);

        verify(projectService).createProject(argThat(project -> project.getName().equals(projectCreateDto.getName())));
    }

    @Test
    public void createProject_shouldReturnException() {
        ProjectCreateDto projectCreateDto = new ProjectCreateDto();
        projectCreateDto.setName(PROJECT_NAME);

        when(projectService.isNotUnique(PROJECT_NAME)).thenReturn(true);

        assertThrows(NotUniqueDataException.class, () -> projectRestController.createProject(projectCreateDto));
    }

    @Test
    public void updateProject() {
        Project project = createProjectObj();
        ProjectCreateDto projectCreateDto = new ProjectCreateDto();
        projectCreateDto.setName(PROJECT_NAME);

        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(project));
        when(projectService.isNotUnique(PROJECT_ID, PROJECT_NAME)).thenReturn(false);

        projectRestController.updateProject(projectCreateDto, PROJECT_ID);

        verify(projectService).updateProject(project);
    }

    @Test
    public void updateProject_shouldReturnResourceNotFoundException() {
        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectRestController.updateProject(any(), PROJECT_ID));
    }

    @Test
    public void updateProject_shouldReturnNotUniqueDataException() {
        ProjectCreateDto projectCreateDto = new ProjectCreateDto();
        projectCreateDto.setName(PROJECT_NAME);

        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(createProjectObj()));
        when(projectService.isNotUnique(PROJECT_ID, PROJECT_NAME)).thenReturn(true);

        assertThrows(NotUniqueDataException.class, () -> projectRestController.updateProject(projectCreateDto, PROJECT_ID));
    }

    @Test
    public void archiveProject() {
        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(createProjectObj()));
        when(trackUnitService.hasNoneFinalTrackUnitForProject(PROJECT_ID)).thenReturn(false);

        projectRestController.archiveProject(PROJECT_ID);

        verify(projectService).archiveProject(PROJECT_ID);
    }

    @Test
    public void archiveProject_shouldReturnResourceNotFoundException() {
        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectRestController.archiveProject(PROJECT_ID));
    }

    @Test
    public void archiveProject_shouldReturnNotProcessedTrackUnitsException() {
        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(createProjectObj()));
        when(trackUnitService.hasNoneFinalTrackUnitForProject(PROJECT_ID)).thenReturn(true);

        assertThrows(NotProcessedTrackUnitsException.class, () -> projectRestController.archiveProject(PROJECT_ID));
    }

    @Test
    public void modifyProjectUser() {
        UserProjectRoleModifyDto userProjectRoleModifyDto = new UserProjectRoleModifyDto();
        userProjectRoleModifyDto.setUserId(USER_ID_ONE);

        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(createProjectObj()));
        when(userService.getNotArchivedUser(USER_ID_ONE)).thenReturn(Optional.of(createUser()));

        projectRestController.modifyProjectUser(userProjectRoleModifyDto, PROJECT_ID);

        verify(relationUserRolesService).updateUserProjectRole(any());
    }

    @Test
    public void modifyProjectUser_shouldReturnResourceNotFoundExceptionForProject() {
        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectRestController.modifyProjectUser(any(), PROJECT_ID));
    }

    @Test
    public void modifyProjectUser_shouldReturnResourceNotFoundExceptionForUser() {
        UserProjectRoleModifyDto userProjectRoleModifyDto = new UserProjectRoleModifyDto();
        userProjectRoleModifyDto.setUserId(USER_ID_ONE);

        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(createProjectObj()));
        when(userService.getNotArchivedUser(USER_ID_ONE)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectRestController
                .modifyProjectUser(userProjectRoleModifyDto, PROJECT_ID));
    }

    @Test
    public void deleteProjectUser() {
        UserProjectRoleDeleteDto userProjectRoleDeleteDto = new UserProjectRoleDeleteDto();
        userProjectRoleDeleteDto.setUserId(USER_ID_ONE);

        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(createProjectObj()));
        when(userService.getNotArchivedUser(USER_ID_ONE)).thenReturn(Optional.of(createUser()));

        projectRestController.deleteProjectUser(userProjectRoleDeleteDto, PROJECT_ID);

        verify(relationUserRolesService).deleteUserProjectRole(USER_ID_ONE, PROJECT_ID);
    }

    @Test
    public void deleteProjectUser_shouldReturnResourceNotFoundExceptionForProject() {
        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectRestController.deleteProjectUser(any(), PROJECT_ID));
    }

    @Test
    public void deleteProjectUser_shouldReturnResourceNotFoundExceptionForUser() {
        UserProjectRoleDeleteDto userProjectRoleDeleteDto = new UserProjectRoleDeleteDto();
        userProjectRoleDeleteDto.setUserId(USER_ID_ONE);

        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(createProjectObj()));
        when(userService.getNotArchivedUser(USER_ID_ONE)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectRestController
                .deleteProjectUser(userProjectRoleDeleteDto, PROJECT_ID));
    }

    @Test
    public void getProjectsOfUser() {
        projectRestController.getProjectsOfUser(
                createSessionUserPrincipal(UserRoleEnum.ROLE_USER, ProjectRoleEnum.EMPLOYEE), USER_ID_ONE);

        verify(projectService).getNotArchivedProjectsOfUserWithRole(USER_ID_ONE);
    }

    @Test
    public void getProjectsOfUser_whenAdmin() {
        projectRestController.getProjectsOfUser(
                createSessionUserPrincipal(UserRoleEnum.ROLE_ADMIN, ProjectRoleEnum.EMPLOYEE), USER_ID_TWO);

        verify(projectService).getNotArchivedProjectsOfUserWithRole(USER_ID_TWO);
    }

    @Test
    public void getProjectsOfUser_shouldReturnForbiddenException() {
        assertThrows(ForbiddenException.class, () -> projectRestController.getProjectsOfUser(
                createSessionUserPrincipal(UserRoleEnum.ROLE_USER, ProjectRoleEnum.EMPLOYEE), USER_ID_TWO));
    }

    @Test
    public void getActiveProjects() {
        when(projectService.getActiveProjects()).thenReturn(List.of(createProjectObj()));

        List<ProjectShortDto> activeProjects = projectRestController.getActiveProjects();

        assertEquals(1, activeProjects.size());
        assertEquals(PROJECT_ID, activeProjects.get(0).getId());
        assertEquals(PROJECT_NAME, activeProjects.get(0).getName());
    }

    private User createUser() {
        User user = new User();
        user.setId(USER_ID_ONE);
        user.setEmail(USER_EMAIL);
        return user;
    }

    private Project createProjectObj() {
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setName(PROJECT_NAME);
        return project;
    }

    private SessionUserPrincipal createSessionUserPrincipal(UserRoleEnum role, ProjectRoleEnum projectRole) {
        SessionUserPrincipal sessionUserPrincipal = new SessionUserPrincipal(USER_ID_ONE, USER_EMAIL);
        UserRole userRole = new UserRole();
        userRole.setUserId(USER_ID_ONE);
        userRole.setRoleId(role);
        UserProjectRole userProjectRole = new UserProjectRole();
        userProjectRole.setProjectId(PROJECT_ID);
        userProjectRole.setUserId(USER_ID_ONE);
        userProjectRole.setProjectRoleId(projectRole);
        sessionUserPrincipal.setAllRoles(List.of(userRole), List.of(userProjectRole));
        return sessionUserPrincipal;
    }

    private QueryArchiveParamRequestDto createProjectParam(String query, boolean archive) {
        QueryArchiveParamRequestDto paramRequest = new QueryArchiveParamRequestDto();
        paramRequest.setQuery(query);
        paramRequest.setArchive(archive);
        return paramRequest;
    }

    private PageableRequestParamDto createPageableParam(int page, int size, ProjectSortFieldEnum sortBy, Sort.Direction sortDirection) {
        PageableRequestParamDto pageableRequestParamDto = new PageableRequestParamDto();
        pageableRequestParamDto.setPage(page);
        pageableRequestParamDto.setSize(size);
        pageableRequestParamDto.setSortBy(sortBy);
        pageableRequestParamDto.setSortDirection(sortDirection);
        return pageableRequestParamDto;
    }
}