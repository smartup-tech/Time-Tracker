package ru.smartup.timetracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.modelmapper.ModelMapper;
import ru.smartup.timetracker.core.SessionUserPrincipal;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.report.response.ReportHoursForProjectsDto;
import ru.smartup.timetracker.dto.report.response.ReportHoursForUsersDto;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.pojo.ReportHours;
import ru.smartup.timetracker.service.ReportService;

import java.sql.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ReportRestControllerTest {
    private static final int USER_ID = 1;
    private static final int PROJECT_ID_1 = 1;
    private static final int PROJECT_ID_2 = 2;
    private static final long TASK_ID = 1;
    private static final float BILLABLE_HOURS = 10;
    private static final float UNBILLABLE_HOURS = 30;
    private static final float TOTAL_HOURS = 40;
    private static final String USER_EMAIL = "email";
    private static final String FIRST_NAME = "first name";
    private static final String LAST_NAME = "last name";
    private static final String PROJECT_NAME = "project name";
    private static final String TASK_NAME = "task name";
    private static final Date START_DATE = Date.valueOf("2022-12-19");
    private static final Date END_DATE = Date.valueOf("2022-12-25");

    private final ReportService reportService = mock(ReportService.class);
    private ReportRestController reportRestController;

    @BeforeEach
    public void setUp() {
        ModelMapper modelMapper = new WebConfig().modelMapper();
        reportRestController = new ReportRestController(reportService, modelMapper);
    }

    @ParameterizedTest
    @EnumSource(value = UserRoleEnum.class, names = {"ROLE_ADMIN", "ROLE_REPORT_RECEIVER"})
    public void getReportHoursForProjects(UserRoleEnum role) {
        when(reportService.getReportHoursForProjects(Set.of(), START_DATE, END_DATE))
                .thenReturn(createListOfReportHours());

        List<ReportHoursForProjectsDto> reportData = reportRestController.getReportHoursForProjects(
                createSessionUserPrincipal(role), START_DATE, END_DATE);

        assertEquals(1, reportData.size());
    }

    @Test
    public void getReportHoursForProjects_whenManager() {
        when(reportService.getReportHoursForProjects(Set.of(PROJECT_ID_1, PROJECT_ID_2), START_DATE, END_DATE))
                .thenReturn(createListOfReportHours());

        List<ReportHoursForProjectsDto> reportData = reportRestController.getReportHoursForProjects(
                createSessionUserPrincipal(ProjectRoleEnum.MANAGER), START_DATE, END_DATE);

        assertEquals(1, reportData.size());
    }

    @ParameterizedTest
    @EnumSource(value = UserRoleEnum.class, names = {"ROLE_ADMIN", "ROLE_REPORT_RECEIVER"})
    public void getReportHoursForUsers(UserRoleEnum role) {
        when(reportService.getReportHoursForUsers(Set.of(), START_DATE, END_DATE))
                .thenReturn(createListOfReportHours());

        List<ReportHoursForUsersDto> reportData = reportRestController.getReportHoursForUsers(
                createSessionUserPrincipal(role), START_DATE, END_DATE);

        assertEquals(1, reportData.size());
    }

    @Test
    public void getReportHoursForUsers_whenManager() {
        when(reportService.getReportHoursForUsers(Set.of(PROJECT_ID_1, PROJECT_ID_2), START_DATE, END_DATE))
                .thenReturn(createListOfReportHours());

        List<ReportHoursForUsersDto> reportData = reportRestController.getReportHoursForUsers(
                createSessionUserPrincipal(ProjectRoleEnum.MANAGER), START_DATE, END_DATE);

        assertEquals(1, reportData.size());
    }

    @Test
    public void getReportHoursForCurrentUser_whenAdmin() {
        when(reportService.getReportHoursForCurrentUser(USER_ID, START_DATE, END_DATE))
                .thenReturn(createListOfReportHours());

        List<ReportHoursForUsersDto> reportData = reportRestController.getReportHoursForCurrentUser(
                createSessionUserPrincipal(UserRoleEnum.ROLE_ADMIN), START_DATE, END_DATE);

        verify(reportService).getReportHoursForCurrentUser(USER_ID, START_DATE, END_DATE);
        assertEquals(1, reportData.size());
    }

    @Test
    public void getReportHoursForCurrentUser_whenUserWithoutProjects() {
        when(reportService.getReportHoursForCurrentUser(USER_ID, START_DATE, END_DATE)).thenReturn(List.of());

        List<ReportHoursForUsersDto> reportData = reportRestController.getReportHoursForCurrentUser(
                createSessionUserPrincipal(UserRoleEnum.ROLE_USER), START_DATE, END_DATE);

        verify(reportService).getReportHoursForCurrentUser(USER_ID, START_DATE, END_DATE);
        assertTrue(reportData.isEmpty());
    }

    @ParameterizedTest
    @EnumSource(value = ProjectRoleEnum.class, names = {"MANAGER", "EMPLOYEE"})
    public void getReportHoursForCurrentUser(ProjectRoleEnum role) {
        when(reportService.getReportHoursForCurrentUser(USER_ID, START_DATE, END_DATE))
                .thenReturn(createListOfReportHours());

        List<ReportHoursForUsersDto> reportData = reportRestController.getReportHoursForCurrentUser(
                createSessionUserPrincipal(role), START_DATE, END_DATE);

        verify(reportService).getReportHoursForCurrentUser(USER_ID, START_DATE, END_DATE);
        assertEquals(1, reportData.size());
    }

    private SessionUserPrincipal createSessionUserPrincipal(UserRoleEnum role) {
        SessionUserPrincipal sessionUserPrincipal = new SessionUserPrincipal(USER_ID, USER_EMAIL);
        UserRole userRole = new UserRole();
        userRole.setUserId(USER_ID);
        userRole.setRoleId(role);
        sessionUserPrincipal.setAllRoles(List.of(userRole), List.of());
        return sessionUserPrincipal;
    }

    private SessionUserPrincipal createSessionUserPrincipal(ProjectRoleEnum projectRole) {
        SessionUserPrincipal sessionUserPrincipal = new SessionUserPrincipal(USER_ID, USER_EMAIL);
        UserRole userRole = new UserRole();
        userRole.setUserId(USER_ID);
        userRole.setRoleId(UserRoleEnum.ROLE_USER);
        UserProjectRole userProjectRole1 = new UserProjectRole();
        userProjectRole1.setProjectId(PROJECT_ID_1);
        userProjectRole1.setUserId(USER_ID);
        userProjectRole1.setProjectRoleId(projectRole);
        UserProjectRole userProjectRole2 = new UserProjectRole();
        userProjectRole2.setProjectId(PROJECT_ID_2);
        userProjectRole2.setUserId(USER_ID);
        userProjectRole2.setProjectRoleId(projectRole);
        sessionUserPrincipal.setAllRoles(List.of(userRole), List.of(userProjectRole1, userProjectRole2));
        return sessionUserPrincipal;
    }

    private List<ReportHours> createListOfReportHours() {
        ReportHours reportHours = new ReportHours();
        reportHours.setProjectId(PROJECT_ID_1);
        reportHours.setProjectName(PROJECT_NAME);
        reportHours.setTaskId(TASK_ID);
        reportHours.setTaskName(TASK_NAME);
        reportHours.setUserId(USER_ID);
        reportHours.setUserFirstName(FIRST_NAME);
        reportHours.setUserLastName(LAST_NAME);
        reportHours.setBillableHours(BILLABLE_HOURS);
        reportHours.setUnbillableHours(UNBILLABLE_HOURS);
        reportHours.setTotalHours(TOTAL_HOURS);
        return List.of(reportHours);
    }
}