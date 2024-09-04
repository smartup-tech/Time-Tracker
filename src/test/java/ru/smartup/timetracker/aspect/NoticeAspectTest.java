package ru.smartup.timetracker.aspect;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.pojo.TrackUnitProjectTask;
import ru.smartup.timetracker.service.ProjectService;
import ru.smartup.timetracker.service.RelationUserRolesService;
import ru.smartup.timetracker.service.TrackUnitService;
import ru.smartup.timetracker.service.UserService;
import ru.smartup.timetracker.service.notification.NoticeScheduleService;
import ru.smartup.timetracker.service.notification.notifier.NotifierObservable;
import ru.smartup.timetracker.utils.FreezeDateUtils;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class NoticeAspectTest {
    private static final int PROJECT_ID = 1;
    private static final String PROJECT_NAME = "project_name";
    private static final int USER_ID_ONE = 1;
    private static final int USER_ID_TWO = 2;
    private static final long TRACK_UNIT_ID = 1;

    private final RelationUserRolesService relationUserRolesService = mock(RelationUserRolesService.class);
    private final NotifierObservable notifierObservable = mock(NotifierObservable.class);
    private final NoticeScheduleService noticeScheduleService = mock(NoticeScheduleService.class);
    private final FreezeDateUtils freezeDateUtils = mock(FreezeDateUtils.class);
    private final ProjectService projectService = mock(ProjectService.class);
    private final UserService userService = mock(UserService.class);
    private final TrackUnitService trackUnitService = mock(TrackUnitService.class);

    private ProjectService proxyProjectService;
    private RelationUserRolesService proxyRelationUserRolesService;
    private TrackUnitService proxyTrackUnitService;

    @BeforeEach
    public void setUp() {
        NoticeAspect noticeAspect = new NoticeAspect(relationUserRolesService, projectService,
                userService, trackUnitService, notifierObservable, noticeScheduleService, freezeDateUtils);
        AspectJProxyFactory proxyFactoryProjectService = new AspectJProxyFactory();
        proxyFactoryProjectService.setTarget(projectService);
        proxyFactoryProjectService.addAspect(noticeAspect);
        proxyProjectService = proxyFactoryProjectService.getProxy();

        AspectJProxyFactory proxyFactoryRelationUserRolesService = new AspectJProxyFactory();
        proxyFactoryRelationUserRolesService.setTarget(relationUserRolesService);
        proxyFactoryRelationUserRolesService.addAspect(noticeAspect);
        proxyRelationUserRolesService = proxyFactoryRelationUserRolesService.getProxy();

        AspectJProxyFactory proxyFactoryTrackUnitService = new AspectJProxyFactory();
        proxyFactoryTrackUnitService.setTarget(trackUnitService);
        proxyFactoryTrackUnitService.addAspect(noticeAspect);
        proxyTrackUnitService = proxyFactoryTrackUnitService.getProxy();
    }

    @Test
    public void sendNoticeUpdateProjectToManagers() {
        Project project = createProject();

        when(relationUserRolesService.getManagerIdsByProjectId(project.getId())).thenReturn(Set.of(USER_ID_ONE));

        proxyProjectService.updateProject(project);

        verify(projectService).getProject(project.getId());
    }

    @Test
    public void sendNoticeUpdateProjectToManagers_whenNoManagers() {
        Project project = createProject();

        when(relationUserRolesService.getManagerIdsByProjectId(project.getId())).thenReturn(Set.of());

        proxyProjectService.updateProject(project);

        verify(projectService).getProject(project.getId());
    }

    @Test
    public void sendNoticeUpdateUserProjectRole_whenNewRole() {
        UserProjectRole userProjectRole = creatUserProjectRole(ProjectRoleEnum.MANAGER);

        when(relationUserRolesService.getUserProjectRole(
                userProjectRole.getUserId(), userProjectRole.getProjectId())).thenReturn(Optional.empty());
        when(projectService.getProject(userProjectRole.getProjectId())).thenReturn(Optional.of(createProject()));

        proxyRelationUserRolesService.updateUserProjectRole(userProjectRole);

        verify(projectService).getProject(userProjectRole.getProjectId());
    }

    @Test
    public void sendNoticeUpdateUserProjectRole_whenRoleChange() {
        UserProjectRole userProjectRole = creatUserProjectRole(ProjectRoleEnum.MANAGER);

        when(relationUserRolesService.getUserProjectRole(
                userProjectRole.getUserId(), userProjectRole.getProjectId()))
                .thenReturn(Optional.of(creatUserProjectRole(ProjectRoleEnum.EMPLOYEE)));
        when(projectService.getProject(userProjectRole.getProjectId())).thenReturn(Optional.of(createProject()));

        proxyRelationUserRolesService.updateUserProjectRole(userProjectRole);

        verify(projectService).getProject(userProjectRole.getProjectId());
    }

    @Test
    public void sendNoticeUpdateUserRoles_whenAdminAdded() {
        UserRole userRole = createUserRole(USER_ID_ONE, UserRoleEnum.ROLE_ADMIN);
        List<UserRole> userRolesNew = List.of(userRole);
        List<UserRole> userRolesBefore = List.of(createUserRole(USER_ID_ONE, UserRoleEnum.ROLE_USER));
        User user = new User();
        user.setId(USER_ID_ONE);

        when(userService.getUserRoles(UserRoleEnum.ROLE_ADMIN))
                .thenReturn(List.of(createUserRole(USER_ID_TWO, UserRoleEnum.ROLE_ADMIN), userRole));
        when(userService.getUserRoles(USER_ID_ONE)).thenReturn(userRolesBefore);
        when(userService.getUser(USER_ID_ONE)).thenReturn(Optional.of(user));

        proxyRelationUserRolesService.updateUserRoles(USER_ID_ONE, userRolesNew);

    }

    @Test
    public void sendNoticeUpdateUserRoles_whenAdminRemoved() {
        List<UserRole> userRolesNew = List.of(createUserRole(USER_ID_ONE, UserRoleEnum.ROLE_USER));
        List<UserRole> userRolesBefore = List.of(createUserRole(USER_ID_ONE, UserRoleEnum.ROLE_ADMIN));
        User user = new User();
        user.setId(USER_ID_ONE);

        when(userService.getUserRoles(UserRoleEnum.ROLE_ADMIN))
                .thenReturn(List.of(createUserRole(USER_ID_TWO, UserRoleEnum.ROLE_ADMIN)));
        when(userService.getUserRoles(USER_ID_ONE)).thenReturn(userRolesBefore);
        when(userService.getUser(USER_ID_ONE)).thenReturn(Optional.of(user));

        proxyRelationUserRolesService.updateUserRoles(USER_ID_ONE, userRolesNew);

    }

    @Test
    public void sendNoticeRejectTracks() {
        TrackUnitProjectTask trackUnitProjectTask = new TrackUnitProjectTask();
        trackUnitProjectTask.setTrackUnitWorkDay(new Date());
        trackUnitProjectTask.setUserId(USER_ID_ONE);

        when(trackUnitService.getTrackUnitsInfo(anyList())).thenReturn(List.of(trackUnitProjectTask));

        proxyTrackUnitService.reject(List.of(TRACK_UNIT_ID), StringUtils.EMPTY);

    }

    @Test
    public void sendNoticeFreezeTracksSuccess() {
        LocalDate now = LocalDate.now();

        when(userService.getUserRoles(Set.of(UserRoleEnum.ROLE_ADMIN, UserRoleEnum.ROLE_REPORT_RECEIVER)))
                .thenReturn(List.of(createUserRole(USER_ID_ONE, UserRoleEnum.ROLE_ADMIN)));
        when(trackUnitService.freezeAllByDate(now)).thenReturn(0);

        proxyTrackUnitService.freezeAllByDate(now);
    }

    @Test
    public void sendNoticeFreezeTracksError() {
        LocalDate now = LocalDate.now();

        when(userService.getUserRoles(UserRoleEnum.ROLE_ADMIN))
                .thenReturn(List.of(createUserRole(USER_ID_ONE, UserRoleEnum.ROLE_ADMIN)));
        when(trackUnitService.freezeAllByDate(now)).thenThrow(new RuntimeException());

        try {
            proxyTrackUnitService.freezeAllByDate(now);
        } catch (Exception ignored) {
        }

    }

    private Project createProject() {
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setName(PROJECT_NAME);
        return project;
    }

    private UserProjectRole creatUserProjectRole(ProjectRoleEnum projectRole) {
        UserProjectRole userProjectRole = new UserProjectRole();
        userProjectRole.setProjectId(PROJECT_ID);
        userProjectRole.setUserId(USER_ID_ONE);
        userProjectRole.setProjectRoleId(projectRole);
        return userProjectRole;
    }

    private UserRole createUserRole(int userId, UserRoleEnum role) {
        UserRole userRole = new UserRole();
        userRole.setRoleId(role);
        userRole.setUserId(userId);
        return userRole;
    }
}