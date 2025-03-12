package ru.smartup.timetracker.controller;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.passay.PasswordGenerator;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.dto.ErrorCode;
import ru.smartup.timetracker.dto.PageableRequestParamDto;
import ru.smartup.timetracker.dto.position.response.PositionDto;
import ru.smartup.timetracker.dto.project.response.ProjectShortDto;
import ru.smartup.timetracker.dto.employee.request.EmployeeCreateDto;
import ru.smartup.timetracker.dto.employee.request.EmployeeUpdateDto;
import ru.smartup.timetracker.dto.QueryArchiveParamRequestDto;
import ru.smartup.timetracker.dto.employee.response.EmployeeDetailDto;
import ru.smartup.timetracker.dto.employee.response.EmployeeShortDto;
import ru.smartup.timetracker.entity.*;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;
import ru.smartup.timetracker.entity.field.sort.EmployeeSortFieldEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.exception.NotProcessedTrackUnitsException;
import ru.smartup.timetracker.exception.NotUniqueDataException;
import ru.smartup.timetracker.exception.RelatedEntitiesFoundException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.pojo.notice.NoticePersonal;
import ru.smartup.timetracker.pojo.notice.NoticePersonalToken;
import ru.smartup.timetracker.service.*;
import ru.smartup.timetracker.service.notification.notifier.NotifierObservable;
import ru.smartup.timetracker.utils.CommonStringUtils;
import ru.smartup.timetracker.validation.validator.PasswordValidator;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class EmployeeRestController {

    private static final int MAX_NUMBER_OF_EMPLOYEES = 20;

    private final EmployeeService employeeService;
    private final PositionService positionService;
    private final RelationEmployeeRolesService relationEmployeeRolesService;
    private final ProjectService projectService;
    private final TrackUnitService trackUnitService;
    private final PasswordResetTokenService passwordResetTokenService;

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotifierObservable notifierObservable;
    private final PasswordGenerator passwordGenerator;
    private final ConversionService conversionService;

    @PreAuthorize("getPrincipal().isAdmin()")
    @GetMapping
    public Page<EmployeeShortDto> getEmployeesByPage(final @Valid QueryArchiveParamRequestDto employeeRequestParam,
                                                     final @Valid PageableRequestParamDto<EmployeeSortFieldEnum> pageableCriteria) {
        pageableCriteria.setSortBy(conversionService.convert(pageableCriteria.getSortBy(), EmployeeSortFieldEnum.class));
        return employeeService.getEmployees(employeeRequestParam, pageableCriteria);
    }

    @PreAuthorize("getPrincipal().isAdmin() or getPrincipal().isManager(#projectId)")
    @GetMapping("/project")
    public List<EmployeeShortDto> getEmployeesForProject(@RequestParam(value = "projectId") int projectId,
                                                         @RequestParam(value = "query", defaultValue = StringUtils.EMPTY) String query) {
        Optional<Project> existProject = projectService.getNotArchivedProject(projectId);
        if (existProject.isEmpty()) {
            throw new ResourceNotFoundException("Active project was not found by projectId = " + projectId + ".");
        }
        Sort sort = Sort.by(Sort.Direction.ASC, EmployeeSortFieldEnum.NAME.getValues());
        Pageable pageable = PageRequest.of(0, MAX_NUMBER_OF_EMPLOYEES, sort);

        return employeeService.getEmployeesForProject(projectId, CommonStringUtils.escapePercentAndUnderscore(query), pageable)
                .stream()
                .map(employee -> modelMapper.map(employee, EmployeeShortDto.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @GetMapping("/{employeeId}")
    public EmployeeDetailDto getEmployee(@PathVariable("employeeId") int employeeId) {
        Optional<Employee> existEmployee = employeeService.getEmployee(employeeId);
        if (existEmployee.isEmpty()) {
            throw new ResourceNotFoundException("Employee was not found by employeeId = " + employeeId + ".");
        }
        Employee employee = existEmployee.get();
        EmployeeDetailDto employeeDetailDto = modelMapper.map(employee, EmployeeDetailDto.class);
        PositionDto positionDto = positionService.getPosition(employee.getPositionId())
                .map(position -> modelMapper.map(position, PositionDto.class))
                .orElse(new PositionDto());
        employeeDetailDto.setPosition(positionDto);
        employeeDetailDto.setRoles(employeeService.getEmployeeRoles(employeeId).stream()
                .map(EmployeeRole::getRoleId)
                .collect(Collectors.toList()));
        Map<Integer, List<ProjectRoleEnum>> projectRoles = employeeService.getEmployeeProjectRoles(employeeId).stream()
                .collect(Collectors.groupingBy(EmployeeProjectRole::getProjectId, Collectors.toList()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(EmployeeProjectRole::getProjectRoleId)
                        .collect(Collectors.toList())));
        employeeDetailDto.setProjectRoles(projectRoles);
        return employeeDetailDto;
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @PostMapping
    public EmployeeShortDto createEmployee(@Valid @RequestBody EmployeeCreateDto employeeCreateDto) {
        Optional<Position> existPosition = positionService.getNotArchivedPosition(employeeCreateDto.getPositionId());
        if (existPosition.isEmpty()) {
            throw new ResourceNotFoundException("Active position was not found by positionId = "
                    + employeeCreateDto.getPositionId() + ".");
        }
        if (employeeService.isNotUnique(employeeCreateDto.getEmail())) {
            throw new NotUniqueDataException(ErrorCode.NOT_UNIQUE_EMPLOYEE_NAME, "Employee with specified email = '"
                    + employeeCreateDto.getEmail() + "' already exists.");
        }
        String password = employeeCreateDto.getPassword() == null
                ? passwordGenerator.generatePassword(PasswordValidator.MAX_PASSWORD_LENGTH, PasswordValidator.getCharacterRules())
                : employeeCreateDto.getPassword();
        Employee employee = modelMapper.map(employeeCreateDto, Employee.class);
        employee.setPasswordHash(passwordEncoder.encode(password));
        int employeeId = employeeService.createEmployee(employee);
        List<EmployeeRole> employeeRoles = employeeCreateDto.getRoles().stream()
                .map(role -> new EmployeeRole(employeeId, role))
                .collect(Collectors.toList());
        relationEmployeeRolesService.updateEmployeeRoles(employeeId, employeeRoles);
        EmployeeShortDto employeeShortDto = modelMapper.map(employeeCreateDto, EmployeeShortDto.class);
        employeeShortDto.setId(employeeId);

        String token = passwordResetTokenService.createPasswordResetTokenForRegistration(employee.getId());
        long ttlInHours = passwordResetTokenService.getPasswordRegistrationTokenTtlInHours();

        final NoticePersonalToken noticePersonalToken = new NoticePersonalToken(employee.getFirstName(), token, ttlInHours);
        final Notice notice = new Notice(NoticeTypeEnum.REGISTER_NEW_EMPLOYEE, noticePersonalToken);

        notifierObservable.notifyEmailChannel(List.of(employee), notice);

        return employeeShortDto;
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @PatchMapping("/{employeeId}")
    public void updateEmployee(@Valid @RequestBody EmployeeUpdateDto employeeUpdateDto, @PathVariable("employeeId") int employeeId) {
        Optional<Employee> existEmployee = employeeService.getNotArchivedEmployee(employeeId);
        if (existEmployee.isEmpty()) {
            throw new ResourceNotFoundException("Active employee was not found by employeeId = " + employeeId + ".");
        }
        Optional<Position> existPosition = positionService.getNotArchivedPosition(employeeUpdateDto.getPositionId());
        if (existPosition.isEmpty()) {
            throw new ResourceNotFoundException("Active position was not found by positionId = "
                    + employeeUpdateDto.getPositionId() + ".");
        }
        Employee employee = existEmployee.get();
        boolean isChangedEmail = false;
        if (!employee.getEmail().equals(employeeUpdateDto.getEmail())) {
            if (employeeService.isNotUnique(employeeUpdateDto.getEmail())) {
                throw new NotUniqueDataException(ErrorCode.NOT_UNIQUE_EMPLOYEE_NAME, "Employee with specified email = '"
                        + employeeUpdateDto.getEmail() + "' already exists.");
            }
            isChangedEmail = true;
        }
        modelMapper.map(employeeUpdateDto, employee);
        boolean isChangedPassword = false;
        String password = employeeUpdateDto.getPassword();
        if (password != null) {
            employee.setPasswordHash(passwordEncoder.encode(password));
            isChangedPassword = true;
        }
        employeeService.updateEmployee(employee);
        List<EmployeeRole> employeeRoles = employeeUpdateDto.getRoles().stream()
                .map(role -> new EmployeeRole(employeeId, role))
                .collect(Collectors.toList());
        relationEmployeeRolesService.updateEmployeeRoles(employeeId, employeeRoles);
        if (isChangedEmail) {
            employeeService.invalidateEmployeeSessions(employeeId);
        }

        if (isChangedPassword) {
            final NoticePersonal noticePersonal = new NoticePersonal(employee.getFirstName());
            final Notice notice = new Notice(NoticeTypeEnum.PASSWORD_RESET,noticePersonal);

            notifierObservable.notifyEmailChannel(List.of(employee), notice);
        }
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @PostMapping("/{employeeId}/archive")
    public void archiveEmployee(@PathVariable("employeeId") int employeeId,
                                @RequestParam(value = "force", defaultValue = "false") boolean force) {
        Optional<Employee> existEmployee = employeeService.getNotArchivedEmployee(employeeId);
        if (existEmployee.isEmpty()) {
            throw new ResourceNotFoundException("Active employee was not found by employeeId = " + employeeId + ".");
        }
        if (trackUnitService.hasNoneFinalTrackUnitForEmployee(employeeId)) {
            throw new NotProcessedTrackUnitsException(ErrorCode.NOT_PROCESSED_TRACK_UNITS_FOR_EMPLOYEE,
                    "Archive is not available now. Please, check all not processed track units of employee; employeeId = "
                            + employeeId + ".");
        }
        if (!force) {
            List<Project> projects = projectService.getNotArchivedProjectsOfEmployee(employeeId);
            if (!CollectionUtils.isEmpty(projects)) {
                List<ProjectShortDto> linkedProjects = projects.stream()
                        .map(project -> modelMapper.map(project, ProjectShortDto.class))
                        .collect(Collectors.toList());
                throw new RelatedEntitiesFoundException(ErrorCode.RELATED_ENTITIES_FOUND_FOR_EMPLOYEE,
                        "The specified employee will be removed from not archived projects; employeeId = " + employeeId + ".",
                        linkedProjects);
            }
        }
        employeeService.updateArchiveStatus(employeeId, true);
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @PostMapping("/{employeeId}/unArchive")
    public void unArchiveEmployee(@PathVariable("employeeId") int employeeId) {
        employeeService.unArchiveEmployee(employeeId);
    }
}
