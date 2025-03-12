package ru.smartup.timetracker.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.*;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.PageableRequestParamDto;
import ru.smartup.timetracker.dto.employee.request.EmployeeCreateDto;
import ru.smartup.timetracker.dto.employee.request.EmployeeUpdateDto;
import ru.smartup.timetracker.dto.QueryArchiveParamRequestDto;
import ru.smartup.timetracker.dto.employee.response.EmployeeShortDto;
import ru.smartup.timetracker.dto.employee.response.EmployeeDetailDto;
import ru.smartup.timetracker.entity.*;
import ru.smartup.timetracker.entity.field.sort.EmployeeSortFieldEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.exception.NotProcessedTrackUnitsException;
import ru.smartup.timetracker.exception.NotUniqueDataException;
import ru.smartup.timetracker.exception.RelatedEntitiesFoundException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.service.*;
import ru.smartup.timetracker.service.notification.notifier.NotifierObservable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EmployeeRestControllerTest {
    private static final int PAGE = 1;
    private static final int SIZE = 10;
    private static final int MAX_NUMBER_OF_EMPLOYEES = 20;
    private static final int PROJECT_ID = 1;
    private static final int POSITION_ID = 1;
    private static final int POSITION_ID_NEW = 2;
    private static final int EMPLOYEE_ID = 1;
    private static final String EMPLOYEE_EMAIL = "employee_email";
    private static final String EMPLOYEE_EMAIL_NEW = "employee_email_new";
    private static final String EMPLOYEE_FIRST_NAME = "employee_first_name";
    private static final String EMPLOYEE_PASSWORD_NEW = "employee_pwd_new";
    private static final String NAME_PROPERTY = "name";
    private static final String PASSWORD_REGISTRATION_LINK = "http://localhost:5173/set-password?token=";
    private static final String EMPLOYEE_REGISTRATION_SUBJECT = "Добро пожаловать";
    private static final String EMPLOYEE_REGISTRATION_TEMPLATE = "employeeRegistration.html";
    private static final String PASSWORD_RESET_SUBJECT = "Сброс пароля";
    private static final String PASSWORD_RESET_TEMPLATE = "passwordReset.html";

    private final EmployeeService employeeService = mock(EmployeeService.class);
    private final PositionService positionService = mock(PositionService.class);
    private final RelationEmployeeRolesService relationEmployeeRolesService = mock(RelationEmployeeRolesService.class);
    private final ProjectService projectService = mock(ProjectService.class);
    private final TrackUnitService trackUnitService = mock(TrackUnitService.class);
    private final PasswordResetTokenService passwordResetTokenService = mock(PasswordResetTokenService.class);
    private final NotifierObservable notifierObservable = mock(NotifierObservable.class);
    private final ConversionService conversionService = mock(ConversionService.class);

    private EmployeeRestController employeeRestController;

    private ModelMapper modelMapper;

    @BeforeEach
    public void setUp() {
        WebConfig webConfig = new WebConfig();
        modelMapper = webConfig.modelMapper();
        employeeRestController = new EmployeeRestController(employeeService, positionService, relationEmployeeRolesService,
                projectService, trackUnitService, passwordResetTokenService, modelMapper, webConfig.passwordEncoder(),
                notifierObservable, webConfig.passwordGenerator(), conversionService);
    }

    @Test
    public void getEmployeesByPage() {
        Page<Employee> employees = new PageImpl<>(List.of(createEmployeeObj()));
        Page<EmployeeShortDto> employeeDtos = employees.map(employee -> modelMapper.map(employee, EmployeeShortDto.class));

        QueryArchiveParamRequestDto employeeParam = createEmployeeParam("", false);
        PageableRequestParamDto pageableParam = createPageableParam(PAGE, SIZE, EmployeeSortFieldEnum.NAME, Sort.Direction.ASC);

        when(employeeService.getEmployees(employeeParam, pageableParam)).thenReturn(employeeDtos);

        when(employeeService.getEmployeesRoles(List.of(EMPLOYEE_ID))).thenReturn(Map.of(EMPLOYEE_ID, List.of(createEmployeeRole())));

        Page<EmployeeShortDto> employeesByPage = employeeRestController.getEmployeesByPage(employeeParam, pageableParam);

        verify(employeeService).getEmployees(employeeParam, pageableParam);

        assertEquals(1, employeesByPage.getTotalElements());
    }

    @Test
    public void getEmployeesByPage_whenSearchQuery() {
        Page<Employee> employees = new PageImpl<>(List.of(createEmployeeObj()));
        Page<EmployeeShortDto> employeeDtos = employees.map(employee -> modelMapper.map(employee, EmployeeShortDto.class));

        QueryArchiveParamRequestDto employeeParam = createEmployeeParam(EMPLOYEE_FIRST_NAME, false);
        PageableRequestParamDto pageableParam = createPageableParam(PAGE, SIZE, EmployeeSortFieldEnum.NAME, Sort.Direction.ASC);

        when(employeeService.getEmployees(employeeParam, pageableParam)).thenReturn(employeeDtos);
        when(employeeService.getEmployeesRoles(List.of(EMPLOYEE_ID))).thenReturn(Map.of(EMPLOYEE_ID, List.of(createEmployeeRole())));

        Page<EmployeeShortDto> employeesByPage = employeeRestController.getEmployeesByPage(employeeParam, pageableParam);

        verify(employeeService).getEmployees(employeeParam, pageableParam);
        assertEquals(1, employeesByPage.getTotalElements());
    }

    @Test
    public void getEmployeesForProject() {
        Pageable pageable = PageRequest.of(0, MAX_NUMBER_OF_EMPLOYEES,
                Sort.by(Sort.Direction.ASC, EmployeeSortFieldEnum.NAME.getValues()));

        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(new Project()));
        when(employeeService.getEmployeesForProject(PROJECT_ID, StringUtils.EMPTY, pageable)).thenReturn(List.of(createEmployeeObj()));

        List<EmployeeShortDto> employeesForProject = employeeRestController.getEmployeesForProject(PROJECT_ID, StringUtils.EMPTY);

        assertEquals(1, employeesForProject.size());
        assertEquals(EMPLOYEE_ID, employeesForProject.get(0).getId());
        assertEquals(EMPLOYEE_EMAIL, employeesForProject.get(0).getEmail());
        assertEquals(EMPLOYEE_FIRST_NAME, employeesForProject.get(0).getFirstName());
    }

    @Test
    public void getEmployeesForProject_shouldReturnResourceNotFoundException() {
        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                employeeRestController.getEmployeesForProject(PROJECT_ID, StringUtils.EMPTY));
    }

    @Test
    public void getEmployee() {
        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(createEmployeeObj()));
        when(positionService.getPosition(POSITION_ID)).thenReturn(Optional.of(createPosition(POSITION_ID)));
        when(employeeService.getEmployeeRoles(EMPLOYEE_ID)).thenReturn(List.of(createEmployeeRole()));
        when(employeeService.getEmployeeProjectRoles(EMPLOYEE_ID)).thenReturn(List.of(createEmployeeProjectRole()));

        EmployeeDetailDto employeeDetailDto = employeeRestController.getEmployee(EMPLOYEE_ID);

        assertEquals(1, employeeDetailDto.getRoles().size());
        assertEquals(EmployeeRoleEnum.ROLE_EMPLOYEE, employeeDetailDto.getRoles().get(0));
        assertEquals(1, employeeDetailDto.getProjectRoles().size());
        assertEquals(ProjectRoleEnum.MANAGER, employeeDetailDto.getProjectRoles().get(PROJECT_ID).get(0));
        assertEquals(EMPLOYEE_ID, employeeDetailDto.getId());
        assertEquals(EMPLOYEE_EMAIL, employeeDetailDto.getEmail());
        assertEquals(EMPLOYEE_FIRST_NAME, employeeDetailDto.getFirstName());
    }

    @Test
    public void getEmployee_shouldReturnResourceNotFoundException() {
        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeRestController.getEmployee(EMPLOYEE_ID));
    }

    @Test
    public void createEmployee() {
        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.of(createPosition(POSITION_ID)));
        when(employeeService.isNotUnique(EMPLOYEE_EMAIL)).thenReturn(false);

        EmployeeCreateDto employeeCreateDto = createEmployeeCreateDto();

        employeeRestController.createEmployee(employeeCreateDto);

        verify(employeeService).createEmployee(argThat(employee -> employee.getEmail().equals(EMPLOYEE_EMAIL)
                && (employee.getPositionId() == POSITION_ID)));
        verify(relationEmployeeRolesService).updateEmployeeRoles(anyInt(), any());
        verify(passwordResetTokenService).createPasswordResetTokenForRegistration(anyInt());

        verify(notifierObservable).notifyEmailChannel(anyList(), any(Notice.class));
    }

    @Test
    public void createEmployee_shouldReturnResourceNotFoundException() {
        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeRestController.createEmployee(createEmployeeCreateDto()));
    }

    @Test
    public void createEmployee_shouldReturnNotUniqueDataException() {
        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.of(createPosition(POSITION_ID)));
        when(employeeService.isNotUnique(EMPLOYEE_EMAIL)).thenReturn(true);

        assertThrows(NotUniqueDataException.class, () -> employeeRestController.createEmployee(createEmployeeCreateDto()));
    }

    @Test
    public void updateEmployee() {
        when(employeeService.getNotArchivedEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(createEmployeeObj()));
        when(positionService.getNotArchivedPosition(POSITION_ID_NEW)).thenReturn(Optional.of(createPosition(POSITION_ID_NEW)));

        employeeRestController.updateEmployee(createEmployeeUpdateDto(EMPLOYEE_EMAIL), EMPLOYEE_ID);

        verify(employeeService).updateEmployee(argThat(employee -> employee.getEmail().equals(EMPLOYEE_EMAIL)
                && (employee.getPositionId() == POSITION_ID_NEW)));
        verify(relationEmployeeRolesService).updateEmployeeRoles(EMPLOYEE_ID, List.of(createEmployeeRole()));
        verify(employeeService, never()).invalidateEmployeeSessions(EMPLOYEE_ID);
    }

    @Test
    public void updateEmployee_shouldReturnResourceNotFoundExceptionForEmployee() {
        when(employeeService.getNotArchivedEmployee(EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeRestController.updateEmployee(
                createEmployeeUpdateDto(EMPLOYEE_EMAIL), EMPLOYEE_ID));
    }

    @Test
    public void updateEmployee_shouldReturnResourceNotFoundExceptionForPosition() {
        when(employeeService.getNotArchivedEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(createEmployeeObj()));
        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeRestController.updateEmployee(
                createEmployeeUpdateDto(EMPLOYEE_EMAIL), EMPLOYEE_ID));
    }

    @Test
    public void updateEmployee_shouldReturnNotUniqueDataException() {
        when(employeeService.getNotArchivedEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(createEmployeeObj()));
        when(positionService.getNotArchivedPosition(POSITION_ID_NEW)).thenReturn(Optional.of(createPosition(POSITION_ID_NEW)));
        when(employeeService.isNotUnique(EMPLOYEE_EMAIL_NEW)).thenReturn(true);

        assertThrows(NotUniqueDataException.class, () -> employeeRestController.updateEmployee(
                createEmployeeUpdateDto(EMPLOYEE_EMAIL_NEW), EMPLOYEE_ID));
    }

    @Test
    public void updateEmployee_whenUpdateEmail() {
        when(employeeService.getNotArchivedEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(createEmployeeObj()));
        when(positionService.getNotArchivedPosition(POSITION_ID_NEW)).thenReturn(Optional.of(createPosition(POSITION_ID_NEW)));
        when(employeeService.isNotUnique(EMPLOYEE_EMAIL_NEW)).thenReturn(false);

        employeeRestController.updateEmployee(createEmployeeUpdateDto(EMPLOYEE_EMAIL_NEW), EMPLOYEE_ID);

        verify(employeeService).updateEmployee(argThat(employee -> employee.getEmail().equals(EMPLOYEE_EMAIL_NEW)
                && (employee.getPositionId() == POSITION_ID_NEW)));
        verify(relationEmployeeRolesService).updateEmployeeRoles(EMPLOYEE_ID, List.of(createEmployeeRole()));
        verify(employeeService).invalidateEmployeeSessions(EMPLOYEE_ID);
    }

    @Test
    public void updateEmployee_whenUpdatePassword() {
        when(employeeService.getNotArchivedEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(createEmployeeObj()));
        when(positionService.getNotArchivedPosition(POSITION_ID_NEW)).thenReturn(Optional.of(createPosition(POSITION_ID_NEW)));
        EmployeeUpdateDto employeeUpdateDto = createEmployeeUpdateDto(EMPLOYEE_EMAIL);
        employeeUpdateDto.setPassword(EMPLOYEE_PASSWORD_NEW);

        employeeRestController.updateEmployee(employeeUpdateDto, EMPLOYEE_ID);

        verify(employeeService).updateEmployee(argThat(employee -> employee.getEmail().equals(EMPLOYEE_EMAIL)
                && (employee.getPositionId() == POSITION_ID_NEW)));
        verify(relationEmployeeRolesService).updateEmployeeRoles(EMPLOYEE_ID, List.of(createEmployeeRole()));
        verify(employeeService, never()).invalidateEmployeeSessions(EMPLOYEE_ID);

        verify(notifierObservable).notifyEmailChannel(anyList(), any(Notice.class));
    }

    @Test
    public void archiveEmployee() {
        when(employeeService.getNotArchivedEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(createEmployeeObj()));
        when(trackUnitService.hasNoneFinalTrackUnitForEmployee(EMPLOYEE_ID)).thenReturn(false);
        when(projectService.getNotArchivedProjectsOfEmployee(EMPLOYEE_ID)).thenReturn(List.of());

        employeeRestController.archiveEmployee(EMPLOYEE_ID, false);

        verify(employeeService).updateArchiveStatus(EMPLOYEE_ID, true);
    }

    @Test
    public void archiveEmployee_shouldReturnRelatedEntitiesFoundException() {
        when(employeeService.getNotArchivedEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(createEmployeeObj()));
        when(trackUnitService.hasNoneFinalTrackUnitForEmployee(EMPLOYEE_ID)).thenReturn(false);
        when(projectService.getNotArchivedProjectsOfEmployee(EMPLOYEE_ID)).thenReturn(List.of(new Project()));

        assertThrows(RelatedEntitiesFoundException.class, () -> employeeRestController.archiveEmployee(EMPLOYEE_ID, false));
    }

    @Test
    public void archiveEmployee_whenForceTrue() {
        when(employeeService.getNotArchivedEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(createEmployeeObj()));
        when(trackUnitService.hasNoneFinalTrackUnitForEmployee(EMPLOYEE_ID)).thenReturn(false);

        employeeRestController.archiveEmployee(EMPLOYEE_ID, true);

        verify(employeeService).updateArchiveStatus(EMPLOYEE_ID, true);
        verify(projectService, never()).getNotArchivedProjectsOfEmployee(EMPLOYEE_ID);
    }

    @Test
    public void archiveEmployee_shouldReturnResourceNotFoundException() {
        when(employeeService.getNotArchivedEmployee(EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeRestController.archiveEmployee(EMPLOYEE_ID, false));
    }

    @Test
    public void archiveEmployee_shouldReturnNotProcessedTrackUnitsException() {
        when(employeeService.getNotArchivedEmployee(EMPLOYEE_ID)).thenReturn(Optional.of(createEmployeeObj()));
        when(trackUnitService.hasNoneFinalTrackUnitForEmployee(EMPLOYEE_ID)).thenReturn(true);

        assertThrows(NotProcessedTrackUnitsException.class, () -> employeeRestController.archiveEmployee(EMPLOYEE_ID, false));
    }

    private Employee createEmployeeObj() {
        Employee employee = new Employee();
        employee.setId(EMPLOYEE_ID);
        employee.setEmail(EMPLOYEE_EMAIL);
        employee.setFirstName(EMPLOYEE_FIRST_NAME);
        employee.setPositionId(POSITION_ID);
        return employee;
    }

    private EmployeeRole createEmployeeRole() {
        EmployeeRole employeeRole = new EmployeeRole();
        employeeRole.setEmployeeId(EMPLOYEE_ID);
        employeeRole.setRoleId(EmployeeRoleEnum.ROLE_EMPLOYEE);
        return employeeRole;
    }

    private EmployeeProjectRole createEmployeeProjectRole() {
        EmployeeProjectRole employeeProjectRole = new EmployeeProjectRole();
        employeeProjectRole.setEmployeeId(EMPLOYEE_ID);
        employeeProjectRole.setProjectId(PROJECT_ID);
        employeeProjectRole.setProjectRoleId(ProjectRoleEnum.MANAGER);
        return employeeProjectRole;
    }

    private Position createPosition(int positionId) {
        Position position = new Position();
        position.setId(positionId);
        return position;
    }

    private EmployeeCreateDto createEmployeeCreateDto() {
        EmployeeCreateDto employeeCreateDto = new EmployeeCreateDto();
        employeeCreateDto.setFirstName(EMPLOYEE_FIRST_NAME);
        employeeCreateDto.setPositionId(POSITION_ID);
        employeeCreateDto.setEmail(EMPLOYEE_EMAIL);
        employeeCreateDto.setPassword(StringUtils.EMPTY);
        employeeCreateDto.setRoles(List.of(EmployeeRoleEnum.ROLE_EMPLOYEE));
        return employeeCreateDto;
    }

    private EmployeeUpdateDto createEmployeeUpdateDto(String email) {
        EmployeeUpdateDto employeeUpdateDto = new EmployeeUpdateDto();
        employeeUpdateDto.setFirstName(EMPLOYEE_FIRST_NAME);
        employeeUpdateDto.setPositionId(POSITION_ID_NEW);
        employeeUpdateDto.setEmail(email);
        employeeUpdateDto.setRoles(List.of(EmployeeRoleEnum.ROLE_EMPLOYEE));
        return employeeUpdateDto;
    }

    private QueryArchiveParamRequestDto createEmployeeParam(String query, boolean archive) {
        QueryArchiveParamRequestDto paramRequest = new QueryArchiveParamRequestDto();
        paramRequest.setQuery(query);
        paramRequest.setArchive(archive);
        return paramRequest;
    }

    private PageableRequestParamDto createPageableParam(int page, int size, EmployeeSortFieldEnum sortBy, Sort.Direction sortDirection) {
        PageableRequestParamDto pageableRequestParamDto = new PageableRequestParamDto();
        pageableRequestParamDto.setPage(page);
        pageableRequestParamDto.setSize(size);
        pageableRequestParamDto.setSortBy(sortBy);
        pageableRequestParamDto.setSortDirection(sortDirection);
        return pageableRequestParamDto;
    }
}