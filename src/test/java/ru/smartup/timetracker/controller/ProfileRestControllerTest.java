package ru.smartup.timetracker.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.profile.request.PasswordUpdateDto;
import ru.smartup.timetracker.dto.profile.request.PersonalDataUpdateDto;
import ru.smartup.timetracker.dto.profile.response.ProfileDto;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.EmployeeProjectRole;
import ru.smartup.timetracker.entity.EmployeeRole;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.exception.ForbiddenException;
import ru.smartup.timetracker.exception.InvalidParameterException;
import ru.smartup.timetracker.service.EmployeeService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProfileRestControllerTest {
    private static final int EMPLOYEE_ID = 1;
    private static final int PROJECT_ID = 1;
    private static final String EMPLOYEE_EMAIL = "employee_email";
    private static final String EMPLOYEE_FIRST_NAME = "employee_first_name";
    private static final String EMPLOYEE_LAST_NAME = "employee_last_name";
    private static final String EMPLOYEE_PASSWORD = "admin";
    private static final String EMPLOYEE_PASSWORD_HASH = "$2y$10$3XCy114Ep7LCnTFqKE8B4OyD7XR3mu/ziGVB8XWYKWRx.sxFXmOe2";

    private final EmployeeService employeeService = mock(EmployeeService.class);
    private ProfileRestController profileRestController;

    @BeforeEach
    public void setUp() {
        WebConfig webConfig = new WebConfig();
        profileRestController = new ProfileRestController(employeeService, webConfig.modelMapper(), webConfig.passwordEncoder());
    }

    @Test
    public void getProfile() {
        Employee employee = createEmployee();

        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

        ProfileDto profileDto = profileRestController.getProfile(createSessionEmployeePrincipal());

        assertEquals(employee.getEmail(), profileDto.getEmail());
        assertEquals(employee.getId(), profileDto.getId());
        assertEquals(1, profileDto.getProjectRoles().size());
        assertTrue(profileDto.getProjectRoles().contains(ProjectRoleEnum.EMPLOYEE));
        assertEquals(1, profileDto.getRoles().size());
        assertTrue(profileDto.getRoles().contains(EmployeeRoleEnum.ROLE_EMPLOYEE));
    }

    @Test
    public void getProfile_shouldReturnException() {
        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> profileRestController.getProfile(createSessionEmployeePrincipal()));
    }

    @Test
    public void updatePersonalData() {
        Employee employee = createEmployee();

        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

        PersonalDataUpdateDto personalDataUpdateDto = new PersonalDataUpdateDto();
        personalDataUpdateDto.setFirstName(EMPLOYEE_FIRST_NAME);
        personalDataUpdateDto.setLastName(EMPLOYEE_LAST_NAME);

        ProfileDto profileDto = profileRestController.updatePersonalData(createSessionEmployeePrincipal(), personalDataUpdateDto);

        assertEquals(employee.getEmail(), profileDto.getEmail());
        assertEquals(employee.getId(), profileDto.getId());
        assertEquals(personalDataUpdateDto.getFirstName(), profileDto.getFirstName());
        assertEquals(personalDataUpdateDto.getLastName(), profileDto.getLastName());
    }

    @Test
    public void updatePersonalData_shouldReturnException() {
        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
                () -> profileRestController.updatePersonalData(createSessionEmployeePrincipal(), null));
    }

    @Test
    public void updatePassword() {
        PasswordUpdateDto passwordUpdateDto = new PasswordUpdateDto();
        passwordUpdateDto.setOldPassword(EMPLOYEE_PASSWORD);
        passwordUpdateDto.setNewPassword(EMPLOYEE_PASSWORD);

        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(createEmployee()));

        profileRestController.updatePassword(createSessionEmployeePrincipal(), passwordUpdateDto);

        verify(employeeService).updatePassword(anyInt(), anyString(), anyString());
    }

    @Test
    public void updatePassword_shouldReturnForbiddenException() {
        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
                () -> profileRestController.updatePassword(createSessionEmployeePrincipal(), null));
    }

    @Test
    public void updatePassword_shouldReturnInvalidParameterException() {
        PasswordUpdateDto passwordUpdateDto = new PasswordUpdateDto();
        passwordUpdateDto.setOldPassword(StringUtils.EMPTY);

        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(createEmployee()));

        assertThrows(InvalidParameterException.class,
                () -> profileRestController.updatePassword(createSessionEmployeePrincipal(), passwordUpdateDto));
    }

    private SessionEmployeePrincipal createSessionEmployeePrincipal() {
        SessionEmployeePrincipal sessionEmployeePrincipal = new SessionEmployeePrincipal(EMPLOYEE_ID, EMPLOYEE_EMAIL);
        EmployeeRole employeeRole = new EmployeeRole();
        employeeRole.setEmployeeId(EMPLOYEE_ID);
        employeeRole.setRoleId(EmployeeRoleEnum.ROLE_EMPLOYEE);
        EmployeeProjectRole employeeProjectRole = new EmployeeProjectRole();
        employeeProjectRole.setEmployeeId(EMPLOYEE_ID);
        employeeProjectRole.setProjectId(PROJECT_ID);
        employeeProjectRole.setProjectRoleId(ProjectRoleEnum.EMPLOYEE);
        sessionEmployeePrincipal.setAllRoles(List.of(employeeRole), List.of(employeeProjectRole));
        return sessionEmployeePrincipal;
    }

    private Employee createEmployee() {
        Employee employee = new Employee();
        employee.setId(EMPLOYEE_ID);
        employee.setEmail(EMPLOYEE_EMAIL);
        employee.setPasswordHash(EMPLOYEE_PASSWORD_HASH);
        return employee;
    }
}