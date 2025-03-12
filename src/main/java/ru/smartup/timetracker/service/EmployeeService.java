package ru.smartup.timetracker.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.dto.PageableRequestParamDto;
import ru.smartup.timetracker.dto.QueryArchiveParamRequestDto;
import ru.smartup.timetracker.dto.employee.response.EmployeeShortDto;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.EmployeeProjectRole;
import ru.smartup.timetracker.entity.EmployeeRole;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.field.sort.EmployeeSortFieldEnum;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.repository.EmployeeProjectRoleRepository;
import ru.smartup.timetracker.repository.EmployeeRepository;
import ru.smartup.timetracker.repository.EmployeeRoleRepository;
import ru.smartup.timetracker.repository.criteria.EmployeeFilterBuilder;
import ru.smartup.timetracker.utils.PageableMaker;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeRoleRepository employeeRoleRepository;
    private final EmployeeProjectRoleRepository employeeProjectRoleRepository;
    private final SessionRegistry sessionRegistry;

    private final PageableMaker pageableMaker;
    private final ModelMapper modelMapper;

    public Page<EmployeeShortDto> getEmployees(final QueryArchiveParamRequestDto employeeParams, final PageableRequestParamDto<EmployeeSortFieldEnum> pageableParams) {

        Page<Employee> employees = getPageableAndFilteredEmployees(employeeParams, pageableParams);

        Map<Integer, List<EmployeeRole>> employeeRolesMap = getEmployeesRoles(employees.getContent());

        return employees
                .map(employee -> modelMapper.map(employee, EmployeeShortDto.class))
                .map(employeeShortDto -> setEmployeeRoleInDto(employeeShortDto, employeeRolesMap));
    }

    private Page<Employee> getPageableAndFilteredEmployees(final QueryArchiveParamRequestDto employeeParams, final PageableRequestParamDto<EmployeeSortFieldEnum> pageableParams) {
        Pageable pageable = pageableMaker.make(pageableParams);
        Specification<Employee> useSpec = getEmployeeFilters(employeeParams.getQuery(), employeeParams.isArchive());
        return getPageableAndFilteredEmployees(useSpec, pageable);
    }

    public Page<Employee> getPageableAndFilteredEmployees(final Specification<Employee> employeeSpec, final Pageable pageable) {
        return employeeRepository.findAll(employeeSpec, pageable);
    }

    private Specification<Employee> getEmployeeFilters(final String searchValue,
                                                       final boolean archive) {
        EmployeeFilterBuilder builder = new EmployeeFilterBuilder();

        builder.addIsArchiveFilter(archive);

        if (!searchValue.isBlank()) {
            builder.addNameFilter(searchValue);
        }

        return builder.buildSpecification();
    }

    private EmployeeShortDto setEmployeeRoleInDto(final EmployeeShortDto dto, final Map<Integer, List<EmployeeRole>> employeeIdToRoles) {
        List<EmployeeRoleEnum> roles = employeeIdToRoles.get(dto.getId())
                .stream()
                .map(EmployeeRole::getRoleId)
                .collect(Collectors.toList());
        dto.setRoles(roles);
        return dto;
    }

    public List<Employee> getEmployeesForProject(int projectId, String searchValue, Pageable pageable) {
        return employeeRepository.findCandidatesForProject(projectId, searchValue, pageable);
    }

    public Optional<Employee> getEmployee(int employeeId) {
        return employeeRepository.findById(employeeId);
    }

    public List<Employee> getEmployees(Collection<Integer> employeeIds) {
        return employeeRepository.findAllById(employeeIds);
    }

    public Optional<Employee> getNotArchivedEmployee(int employeeId) {
        return employeeRepository.findByIdAndIsArchivedFalse(employeeId);
    }

    public List<Employee> getNotArchivedEmployees() {
        return employeeRepository.findAllByIsArchivedFalse();
    }

    public Optional<Employee> getArchivedEmployee(final int employeeId) {
        return employeeRepository
                .findByIdAndIsArchivedTrue(employeeId);
    }

    public Optional<Employee> getNotArchivedEmployeeByEmail(String email) {
        return employeeRepository.findByEmailAndIsArchivedFalse(email);
    }


    public List<Employee> getEmployeesByRoles(final List<EmployeeRoleEnum> employeeRoles) {
        return employeeRepository.findAllByEmployeeRoles(employeeRoles);
    }

    public List<Employee> getEmployeesByRole(final EmployeeRoleEnum employeeRoleEnum) {
        return employeeRepository.findAllByEmployeeRole(employeeRoleEnum);
    }

    public List<Employee> getEmployeesByProjectAndProjectRole(final int projectId, final ProjectRoleEnum employeeProjectRole) {
        return employeeRepository.findAllByProjectIdAndProjectRole(projectId, employeeProjectRole);
    }

    public List<EmployeeRole> getEmployeeRoles(int employeeId) {
        return employeeRoleRepository.findAllByEmployeeId(employeeId);
    }

    public List<EmployeeRole> getEmployeeRoles(EmployeeRoleEnum roleId) {
        return employeeRoleRepository.findAllByRoleId(roleId);
    }

    public List<EmployeeRole> getEmployeeRoles(Set<EmployeeRoleEnum> roleIds) {
        return employeeRoleRepository.findAllByRoleIdIn(roleIds);
    }

    public Map<Integer, List<EmployeeRole>> getEmployeesRoles(final Collection<Employee> employees) {
        List<Integer> employeeIds = employees.stream()
                .map(Employee::getId)
                .collect(Collectors.toList());
        return getEmployeesRoles(employeeIds);
    }

    public Map<Integer, List<EmployeeRole>> getEmployeesRoles(List<Integer> employeeIds) {
        return employeeRoleRepository.findAllByEmployeeIdIn(employeeIds).stream()
                .collect(Collectors.groupingBy(EmployeeRole::getEmployeeId, Collectors.toList()));
    }

    public List<EmployeeProjectRole> getEmployeeProjectRoles(int employeeId) {
        return employeeProjectRoleRepository.findAllByEmployeeId(employeeId);
    }

    public List<Employee> getNotArchivedEmployeesWithPosition(int positionId) {
        return employeeRepository.findByPositionIdAndIsArchivedFalse(positionId);
    }

    /**
     * Получить данные пользователей проекта
     *
     * @param projectId идентификатор проекта
     * @return List<EmployeeInProject>
     */
    public List<Employee> getEmployeesFromProject(int projectId) {
        return employeeRepository.findAllEmployeesInProject(projectId);
    }

    public boolean isNotUnique(String email) {
        return employeeRepository.isNotUnique(email);
    }

    @Transactional
    public int createEmployee(Employee employee) {
        return employeeRepository.save(employee).getId();
    }

    @Transactional
    public void updateEmployee(Employee employee) {
        employeeRepository.save(employee);
    }

    @Transactional
    public void updateArchiveStatus(int employeeId, boolean archived) {
        if (archived) {
            employeeProjectRoleRepository.deleteFromNotArchivedProjectsByEmployeeId(employeeId);
            employeeRepository.updateArchiveStatus(employeeId, archived);
            invalidateEmployeeSessions(employeeId);
        } else {
            employeeRepository.updateArchiveStatus(employeeId, archived);
        }
    }

    public void invalidateEmployeeSessions(int employeeId) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            SessionEmployeePrincipal sessionEmployeePrincipal = (SessionEmployeePrincipal) principal;
            if (sessionEmployeePrincipal.getId() == employeeId) {
                sessionRegistry.getAllSessions(sessionEmployeePrincipal, false)
                        .forEach(SessionInformation::expireNow);
                break;
            }
        }
    }

    @Transactional
    public void updatePassword(int employeeId, String oldPasswordHash, String newPasswordHash) {
        employeeRepository.updatePassword(employeeId, oldPasswordHash, newPasswordHash);
    }

    public List<Employee> searchEmployees(final String searchValue, final boolean archive, final Sort sort) {
        Specification<Employee> spec = getEmployeeFilters(searchValue, archive);
        return employeeRepository.findAll(spec, sort);
    }

    public List<Employee> searchEmployeesFromProjects(Set<Integer> projectIds, String searchValue, boolean archive, Sort sort) {
        return employeeRepository.findAllInProjectsByFirstNameOrLastNameAndArchive(projectIds, searchValue, archive, sort);
    }

    public void unArchiveEmployee(int employeeId) {
        getArchivedEmployee(employeeId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Archived employee was not found by employeeId = " + employeeId + ".")
                );

        updateArchiveStatus(employeeId, false);
    }
}
