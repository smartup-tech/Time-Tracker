package ru.smartup.timetracker.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.smartup.timetracker.entity.EmployeeProjectRole;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.EmployeeRole;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode(exclude = {"email", "employeeRoles", "projectIdsByProjectRoles"})
public class SessionEmployeePrincipal {
    private final int id;

    private final String email;

    private Set<EmployeeRoleEnum> employeeRoles;

    /**
     * Содержит идентификаторы проектов пользователя с разбивкой по проектным ролям
     */
    private Map<ProjectRoleEnum, Set<Integer>> projectIdsByProjectRoles;

    public SessionEmployeePrincipal(int id, String email) {
        this.id = id;
        this.email = email;
    }

    public void setAllRoles(List<EmployeeRole> employeeRoles, List<EmployeeProjectRole> employeeProjectRoles) {
        this.employeeRoles = employeeRoles.stream()
                .map(EmployeeRole::getRoleId)
                .collect(Collectors.toSet());
        projectIdsByProjectRoles = employeeProjectRoles.stream()
                .collect(Collectors.groupingBy(EmployeeProjectRole::getProjectRoleId, Collectors.toSet()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().map(EmployeeProjectRole::getProjectId)
                        .collect(Collectors.toSet())));
    }

    /**
     * Является администратором
     *
     * @return boolean
     */
    public boolean isAdmin() {
        return employeeRoles.contains(EmployeeRoleEnum.ROLE_ADMIN);
    }

    /**
     * Имеет право получать отчеты
     *
     * @return boolean
     */
    public boolean isReportReceiver() {
        return employeeRoles.contains(EmployeeRoleEnum.ROLE_REPORT_RECEIVER);
    }

    /**
     * Является пользователем
     *
     * @return boolean
     */
    public boolean isEmployee() {
        return employeeRoles.contains(EmployeeRoleEnum.ROLE_EMPLOYEE);
    }

    /**
     * Является менеджером хотя бы в одном проекте
     *
     * @return boolean
     */
    public boolean isManager() {
        return employeeRoles.contains(EmployeeRoleEnum.ROLE_EMPLOYEE)
                && projectIdsByProjectRoles.containsKey(ProjectRoleEnum.MANAGER);
    }

    /**
     * Не является менеджером в заданном проекте
     *
     * @param projectId идентификатор проекта
     * @return boolean
     */
    public boolean isNotManager(int projectId) {
        return employeeRoles.contains(EmployeeRoleEnum.ROLE_EMPLOYEE)
                && (!projectIdsByProjectRoles.containsKey(ProjectRoleEnum.MANAGER)
                || !projectIdsByProjectRoles.get(ProjectRoleEnum.MANAGER).contains(projectId));
    }

    /**
     * Является менеджером в заданном проекте
     *
     * @param projectId идентификатор проекта
     * @return boolean
     */
    public boolean isManager(int projectId) {
        return employeeRoles.contains(EmployeeRoleEnum.ROLE_EMPLOYEE)
                && projectIdsByProjectRoles.containsKey(ProjectRoleEnum.MANAGER)
                && projectIdsByProjectRoles.get(ProjectRoleEnum.MANAGER).contains(projectId);
    }

    /**
     * Является сотрудником или менеджером в заданном проекте
     *
     * @param projectId идентификатор проекта
     * @return boolean
     */
    public boolean isEmployeeOrManager(int projectId) {
        return employeeRoles.contains(EmployeeRoleEnum.ROLE_EMPLOYEE)
                && ((projectIdsByProjectRoles.containsKey(ProjectRoleEnum.EMPLOYEE)
                && projectIdsByProjectRoles.get(ProjectRoleEnum.EMPLOYEE).contains(projectId))
                || (projectIdsByProjectRoles.containsKey(ProjectRoleEnum.MANAGER)
                && projectIdsByProjectRoles.get(ProjectRoleEnum.MANAGER).contains(projectId)));
    }

    /**
     * Не является менеджером или сотрудником в заданном проекте
     *
     * @param projectId идентификатор проекта
     * @return boolean
     */
    public boolean isNotManagerOrEmployee(int projectId) {
        return employeeRoles.contains(EmployeeRoleEnum.ROLE_EMPLOYEE)
                && (!projectIdsByProjectRoles.containsKey(ProjectRoleEnum.MANAGER)
                || !projectIdsByProjectRoles.get(ProjectRoleEnum.MANAGER).contains(projectId))
                && (!projectIdsByProjectRoles.containsKey(ProjectRoleEnum.EMPLOYEE)
                || !projectIdsByProjectRoles.get(ProjectRoleEnum.EMPLOYEE).contains(projectId));
    }

    /**
     * Является сотрудником в заданном проекте
     *
     * @param projectId идентификатор проекта
     * @return boolean
     */
    public boolean isEmployee(int projectId) {
        return employeeRoles.contains(EmployeeRoleEnum.ROLE_EMPLOYEE)
                && projectIdsByProjectRoles.containsKey(ProjectRoleEnum.EMPLOYEE)
                && projectIdsByProjectRoles.get(ProjectRoleEnum.EMPLOYEE).contains(projectId);
    }

    public Set<Integer> getProjectIdsByProjectRole(ProjectRoleEnum projectRole) {
        return projectIdsByProjectRoles.getOrDefault(projectRole, Set.of());
    }

    public Set<Integer> getTrackableProjectIds() {
        Set<Integer> projectIds = new HashSet<>();
        projectIds.addAll(getProjectIdsByProjectRole(ProjectRoleEnum.EMPLOYEE));
        projectIds.addAll(getProjectIdsByProjectRole(ProjectRoleEnum.MANAGER));
        return projectIds;
    }
}
