package ru.smartup.timetracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.modelmapper.ModelMapper;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.report.response.ReportHoursForProjectsDto;
import ru.smartup.timetracker.dto.report.response.ReportHoursForEmployeesDto;
import ru.smartup.timetracker.entity.EmployeeRole;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.EmployeeProjectRole;
import ru.smartup.timetracker.pojo.ReportHours;
import ru.smartup.timetracker.service.ReportService;

import java.sql.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ReportRestControllerTest {
    private static final int EMPLOYEE_ID = 1;
    private static final int PROJECT_ID_1 = 1;
    private static final int PROJECT_ID_2 = 2;
    private static final long TASK_ID = 1;
    private static final float BILLABLE_HOURS = 10;
    private static final float UNBILLABLE_HOURS = 30;
    private static final float TOTAL_HOURS = 40;
    private static final String EMPLOYEE_EMAIL = "email";
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
    @EnumSource(value = EmployeeRoleEnum.class, names = {"ROLE_ADMIN", "ROLE_REPORT_RECEIVER"})
    public void getReportHoursForProjects(EmployeeRoleEnum role) {
        when(reportService.getReportHoursForProjects(Set.of(), START_DATE, END_DATE))
                .thenReturn(createListOfReportHours());

        List<ReportHoursForProjectsDto> reportData = reportRestController.getReportHoursForProjects(
                createSessionEmployeePrincipal(role), START_DATE, END_DATE);

        assertEquals(1, reportData.size());
    }

    @Test
    public void getReportHoursForProjects_whenManager() {
        when(reportService.getReportHoursForProjects(Set.of(PROJECT_ID_1, PROJECT_ID_2), START_DATE, END_DATE))
                .thenReturn(createListOfReportHours());

        List<ReportHoursForProjectsDto> reportData = reportRestController.getReportHoursForProjects(
                createSessionEmployeePrincipal(ProjectRoleEnum.MANAGER), START_DATE, END_DATE);

        assertEquals(1, reportData.size());
    }

    @ParameterizedTest
    @EnumSource(value = EmployeeRoleEnum.class, names = {"ROLE_ADMIN", "ROLE_REPORT_RECEIVER"})
    public void getReportHoursForEmployees(EmployeeRoleEnum role) {
        when(reportService.getReportHoursForEmployees(Set.of(), START_DATE, END_DATE))
                .thenReturn(createListOfReportHours());

        List<ReportHoursForEmployeesDto> reportData = reportRestController.getReportHoursForEmployees(
                createSessionEmployeePrincipal(role), START_DATE, END_DATE);

        assertEquals(1, reportData.size());
    }

    @Test
    public void getReportHoursForEmployees_whenManager() {
        when(reportService.getReportHoursForEmployees(Set.of(PROJECT_ID_1, PROJECT_ID_2), START_DATE, END_DATE))
                .thenReturn(createListOfReportHours());

        List<ReportHoursForEmployeesDto> reportData = reportRestController.getReportHoursForEmployees(
                createSessionEmployeePrincipal(ProjectRoleEnum.MANAGER), START_DATE, END_DATE);

        assertEquals(1, reportData.size());
    }

    @Test
    public void getReportHoursForCurrentEmployee_whenAdmin() {
        when(reportService.getReportHoursForCurrentEmployee(EMPLOYEE_ID, START_DATE, END_DATE))
                .thenReturn(createListOfReportHours());

        List<ReportHoursForEmployeesDto> reportData = reportRestController.getReportHoursForCurrentEmployee(
                createSessionEmployeePrincipal(EmployeeRoleEnum.ROLE_ADMIN), START_DATE, END_DATE);

        verify(reportService).getReportHoursForCurrentEmployee(EMPLOYEE_ID, START_DATE, END_DATE);
        assertEquals(1, reportData.size());
    }

    @Test
    public void getReportHoursForCurrentEmployee_whenEmployeeWithoutProjects() {
        when(reportService.getReportHoursForCurrentEmployee(EMPLOYEE_ID, START_DATE, END_DATE)).thenReturn(List.of());

        List<ReportHoursForEmployeesDto> reportData = reportRestController.getReportHoursForCurrentEmployee(
                createSessionEmployeePrincipal(EmployeeRoleEnum.ROLE_EMPLOYEE), START_DATE, END_DATE);

        verify(reportService).getReportHoursForCurrentEmployee(EMPLOYEE_ID, START_DATE, END_DATE);
        assertTrue(reportData.isEmpty());
    }

    @ParameterizedTest
    @EnumSource(value = ProjectRoleEnum.class, names = {"MANAGER", "EMPLOYEE"})
    public void getReportHoursForCurrentEmployee(ProjectRoleEnum role) {
        when(reportService.getReportHoursForCurrentEmployee(EMPLOYEE_ID, START_DATE, END_DATE))
                .thenReturn(createListOfReportHours());

        List<ReportHoursForEmployeesDto> reportData = reportRestController.getReportHoursForCurrentEmployee(
                createSessionEmployeePrincipal(role), START_DATE, END_DATE);

        verify(reportService).getReportHoursForCurrentEmployee(EMPLOYEE_ID, START_DATE, END_DATE);
        assertEquals(1, reportData.size());
    }

    private SessionEmployeePrincipal createSessionEmployeePrincipal(EmployeeRoleEnum role) {
        SessionEmployeePrincipal sessionEmployeePrincipal = new SessionEmployeePrincipal(EMPLOYEE_ID, EMPLOYEE_EMAIL);
        EmployeeRole employeeRole = new EmployeeRole();
        employeeRole.setEmployeeId(EMPLOYEE_ID);
        employeeRole.setRoleId(role);
        sessionEmployeePrincipal.setAllRoles(List.of(employeeRole), List.of());
        return sessionEmployeePrincipal;
    }

    private SessionEmployeePrincipal createSessionEmployeePrincipal(ProjectRoleEnum projectRole) {
        SessionEmployeePrincipal sessionEmployeePrincipal = new SessionEmployeePrincipal(EMPLOYEE_ID, EMPLOYEE_EMAIL);
        EmployeeRole employeeRole = new EmployeeRole();
        employeeRole.setEmployeeId(EMPLOYEE_ID);
        employeeRole.setRoleId(EmployeeRoleEnum.ROLE_EMPLOYEE);
        EmployeeProjectRole employeeProjectRole1 = new EmployeeProjectRole();
        employeeProjectRole1.setProjectId(PROJECT_ID_1);
        employeeProjectRole1.setEmployeeId(EMPLOYEE_ID);
        employeeProjectRole1.setProjectRoleId(projectRole);
        EmployeeProjectRole employeeProjectRole2 = new EmployeeProjectRole();
        employeeProjectRole2.setProjectId(PROJECT_ID_2);
        employeeProjectRole2.setEmployeeId(EMPLOYEE_ID);
        employeeProjectRole2.setProjectRoleId(projectRole);
        sessionEmployeePrincipal.setAllRoles(List.of(employeeRole), List.of(employeeProjectRole1, employeeProjectRole2));
        return sessionEmployeePrincipal;
    }

    private List<ReportHours> createListOfReportHours() {
        ReportHours reportHours = new ReportHours();
        reportHours.setProjectId(PROJECT_ID_1);
        reportHours.setProjectName(PROJECT_NAME);
        reportHours.setTaskId(TASK_ID);
        reportHours.setTaskName(TASK_NAME);
        reportHours.setEmployeeId(EMPLOYEE_ID);
        reportHours.setEmployeeFirstName(FIRST_NAME);
        reportHours.setEmployeeLastName(LAST_NAME);
        reportHours.setBillableHours(BILLABLE_HOURS);
        reportHours.setUnbillableHours(UNBILLABLE_HOURS);
        reportHours.setTotalHours(TOTAL_HOURS);
        return List.of(reportHours);
    }
}