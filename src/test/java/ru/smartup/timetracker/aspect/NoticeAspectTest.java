package ru.smartup.timetracker.aspect;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import ru.smartup.timetracker.dto.notice.NoticeCreationType;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.EmployeeProjectRole;
import ru.smartup.timetracker.entity.EmployeeRole;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.pojo.TrackUnitProjectTask;
import ru.smartup.timetracker.service.EmployeeService;
import ru.smartup.timetracker.service.ProjectService;
import ru.smartup.timetracker.service.RelationEmployeeRolesService;
import ru.smartup.timetracker.service.TrackUnitService;
import ru.smartup.timetracker.service.notification.FreezeTracksSuccessNoticeCreationService;
import ru.smartup.timetracker.service.notification.NoticeScheduleService;
import ru.smartup.timetracker.service.notification.notifier.NotifierObservable;
import ru.smartup.timetracker.service.notification.strategy.AdminFreezeSuccessNoticeCreationStrategy;
import ru.smartup.timetracker.service.notification.strategy.NoticeCreationStrategy;
import ru.smartup.timetracker.service.notification.strategy.ReportReceiverFreezeSuccessNoticeCreationStrategy;
import ru.smartup.timetracker.utils.FreezeDateUtils;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NoticeAspectTest {
    private static final int PROJECT_ID = 1;
    private static final String PROJECT_NAME = "project_name";
    private static final int EMPLOYEE_ID_ONE = 1;
    private static final int EMPLOYEE_ID_TWO = 2;
    private static final long TRACK_UNIT_ID = 1;

    private final RelationEmployeeRolesService relationEmployeeRolesService = mock(RelationEmployeeRolesService.class);
    private final NotifierObservable notifierObservable = mock(NotifierObservable.class);
    private final NoticeScheduleService noticeScheduleService = mock(NoticeScheduleService.class);
    private final FreezeDateUtils freezeDateUtils = mock(FreezeDateUtils.class);
    private final ProjectService projectService = mock(ProjectService.class);
    private final EmployeeService employeeService = mock(EmployeeService.class);
    private final TrackUnitService trackUnitService = mock(TrackUnitService.class);

    private ProjectService proxyProjectService;
    private RelationEmployeeRolesService proxyRelationEmployeeRolesService;
    private TrackUnitService proxyTrackUnitService;

    @BeforeEach
    public void setUp() {
        List<NoticeCreationStrategy> strategies = List.of(new AdminFreezeSuccessNoticeCreationStrategy(), new ReportReceiverFreezeSuccessNoticeCreationStrategy());

        FreezeTracksSuccessNoticeCreationService freezeTracksSuccessNoticeCreationService = new FreezeTracksSuccessNoticeCreationService(strategies);
        NoticeAspect noticeAspect = new NoticeAspect(relationEmployeeRolesService, projectService,
                employeeService, trackUnitService, notifierObservable, noticeScheduleService, freezeTracksSuccessNoticeCreationService, freezeDateUtils);
        AspectJProxyFactory proxyFactoryProjectService = new AspectJProxyFactory();
        proxyFactoryProjectService.setTarget(projectService);
        proxyFactoryProjectService.addAspect(noticeAspect);
        proxyProjectService = proxyFactoryProjectService.getProxy();

        AspectJProxyFactory proxyFactoryRelationEmployeeRolesService = new AspectJProxyFactory();
        proxyFactoryRelationEmployeeRolesService.setTarget(relationEmployeeRolesService);
        proxyFactoryRelationEmployeeRolesService.addAspect(noticeAspect);
        proxyRelationEmployeeRolesService = proxyFactoryRelationEmployeeRolesService.getProxy();

        AspectJProxyFactory proxyFactoryTrackUnitService = new AspectJProxyFactory();
        proxyFactoryTrackUnitService.setTarget(trackUnitService);
        proxyFactoryTrackUnitService.addAspect(noticeAspect);
        proxyTrackUnitService = proxyFactoryTrackUnitService.getProxy();
    }

    @Test
    public void sendNoticeUpdateProjectToManagers() {
        Project project = createProject();

        when(relationEmployeeRolesService.getManagerIdsByProjectId(project.getId())).thenReturn(Set.of(EMPLOYEE_ID_ONE));

        proxyProjectService.updateProject(project);

        verify(projectService).getProject(project.getId());
    }

    @Test
    public void sendNoticeUpdateProjectToManagers_whenNoManagers() {
        Project project = createProject();

        when(relationEmployeeRolesService.getManagerIdsByProjectId(project.getId())).thenReturn(Set.of());

        proxyProjectService.updateProject(project);

        verify(projectService).getProject(project.getId());
    }

    @Test
    public void sendNoticeUpdateEmployeeProjectRole_whenNewRole() {
        EmployeeProjectRole employeeProjectRole = creatEmployeeProjectRole(ProjectRoleEnum.MANAGER);

        when(relationEmployeeRolesService.getEmployeeProjectRole(
                employeeProjectRole.getEmployeeId(), employeeProjectRole.getProjectId())).thenReturn(Optional.empty());
        when(projectService.getProject(employeeProjectRole.getProjectId())).thenReturn(Optional.of(createProject()));

        proxyRelationEmployeeRolesService.updateEmployeeProjectRole(employeeProjectRole);

        verify(projectService).getProject(employeeProjectRole.getProjectId());
    }

    @Test
    public void sendNoticeUpdateEmployeeProjectRole_whenRoleChange() {
        EmployeeProjectRole employeeProjectRole = creatEmployeeProjectRole(ProjectRoleEnum.MANAGER);

        when(relationEmployeeRolesService.getEmployeeProjectRole(
                employeeProjectRole.getEmployeeId(), employeeProjectRole.getProjectId()))
                .thenReturn(Optional.of(creatEmployeeProjectRole(ProjectRoleEnum.EMPLOYEE)));
        when(projectService.getProject(employeeProjectRole.getProjectId())).thenReturn(Optional.of(createProject()));

        proxyRelationEmployeeRolesService.updateEmployeeProjectRole(employeeProjectRole);

        verify(projectService).getProject(employeeProjectRole.getProjectId());
    }

    @Test
    public void sendNoticeUpdateEmployeeRoles_whenAdminAdded() {
        EmployeeRole employeeRole = createEmployeeRole(EMPLOYEE_ID_ONE, EmployeeRoleEnum.ROLE_ADMIN);
        List<EmployeeRole> employeeRolesNew = List.of(employeeRole);
        List<EmployeeRole> employeeRolesBefore = List.of(createEmployeeRole(EMPLOYEE_ID_ONE, EmployeeRoleEnum.ROLE_EMPLOYEE));
        Employee employee = new Employee();
        employee.setId(EMPLOYEE_ID_ONE);

        when(employeeService.getEmployeeRoles(EmployeeRoleEnum.ROLE_ADMIN))
                .thenReturn(List.of(createEmployeeRole(EMPLOYEE_ID_TWO, EmployeeRoleEnum.ROLE_ADMIN), employeeRole));
        when(employeeService.getEmployeeRoles(EMPLOYEE_ID_ONE)).thenReturn(employeeRolesBefore);
        when(employeeService.getEmployee(EMPLOYEE_ID_ONE)).thenReturn(Optional.of(employee));

        proxyRelationEmployeeRolesService.updateEmployeeRoles(EMPLOYEE_ID_ONE, employeeRolesNew);

    }

    @Test
    public void sendNoticeUpdateEmployeeRoles_whenAdminRemoved() {
        List<EmployeeRole> employeeRolesNew = List.of(createEmployeeRole(EMPLOYEE_ID_ONE, EmployeeRoleEnum.ROLE_EMPLOYEE));
        List<EmployeeRole> employeeRolesBefore = List.of(createEmployeeRole(EMPLOYEE_ID_ONE, EmployeeRoleEnum.ROLE_ADMIN));
        Employee employee = new Employee();
        employee.setId(EMPLOYEE_ID_ONE);

        when(employeeService.getEmployeeRoles(EmployeeRoleEnum.ROLE_ADMIN))
                .thenReturn(List.of(createEmployeeRole(EMPLOYEE_ID_TWO, EmployeeRoleEnum.ROLE_ADMIN)));
        when(employeeService.getEmployeeRoles(EMPLOYEE_ID_ONE)).thenReturn(employeeRolesBefore);
        when(employeeService.getEmployee(EMPLOYEE_ID_ONE)).thenReturn(Optional.of(employee));

        proxyRelationEmployeeRolesService.updateEmployeeRoles(EMPLOYEE_ID_ONE, employeeRolesNew);

    }

    @Test
    public void sendNoticeRejectTracks() {
        TrackUnitProjectTask trackUnitProjectTask = new TrackUnitProjectTask();
        trackUnitProjectTask.setTrackUnitWorkDay(new Date());
        trackUnitProjectTask.setEmployeeId(EMPLOYEE_ID_ONE);

        when(trackUnitService.getTrackUnitsInfo(anyList())).thenReturn(List.of(trackUnitProjectTask));

        proxyTrackUnitService.reject(List.of(TRACK_UNIT_ID), StringUtils.EMPTY);

    }

    @Test
    public void sendNoticeFreezeTracksSuccess() {
        LocalDate now = LocalDate.now();

        when(employeeService.getEmployeeRoles(Set.of(EmployeeRoleEnum.ROLE_ADMIN, EmployeeRoleEnum.ROLE_REPORT_RECEIVER)))
                .thenReturn(List.of(createEmployeeRole(EMPLOYEE_ID_ONE, EmployeeRoleEnum.ROLE_ADMIN)));
        when(trackUnitService.freezeAllByDate(now)).thenReturn(0);

        proxyTrackUnitService.freezeAllByDate(now);
    }

    @Test
    public void sendNoticeFreezeTracksError() {
        LocalDate now = LocalDate.now();

        when(employeeService.getEmployeeRoles(EmployeeRoleEnum.ROLE_ADMIN))
                .thenReturn(List.of(createEmployeeRole(EMPLOYEE_ID_ONE, EmployeeRoleEnum.ROLE_ADMIN)));
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

    private EmployeeProjectRole creatEmployeeProjectRole(ProjectRoleEnum projectRole) {
        EmployeeProjectRole employeeProjectRole = new EmployeeProjectRole();
        employeeProjectRole.setProjectId(PROJECT_ID);
        employeeProjectRole.setEmployeeId(EMPLOYEE_ID_ONE);
        employeeProjectRole.setProjectRoleId(projectRole);
        return employeeProjectRole;
    }

    private EmployeeRole createEmployeeRole(int employeeId, EmployeeRoleEnum role) {
        EmployeeRole employeeRole = new EmployeeRole();
        employeeRole.setRoleId(role);
        employeeRole.setEmployeeId(employeeId);
        return employeeRole;
    }
}