package ru.smartup.timetracker.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode(exclude = {"email", "userRoles", "projectIdsByProjectRoles"})
public class SessionUserPrincipal {
    private final int id;

    private final String email;

    private Set<UserRoleEnum> userRoles;

    /**
     * Содержит идентификаторы проектов пользователя с разбивкой по проектным ролям
     */
    private Map<ProjectRoleEnum, Set<Integer>> projectIdsByProjectRoles;

    public SessionUserPrincipal(int id, String email) {
        this.id = id;
        this.email = email;
    }

    public void setAllRoles(List<UserRole> userRoles, List<UserProjectRole> userProjectRoles) {
        this.userRoles = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toSet());
        projectIdsByProjectRoles = userProjectRoles.stream()
                .collect(Collectors.groupingBy(UserProjectRole::getProjectRoleId, Collectors.toSet()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().map(UserProjectRole::getProjectId)
                        .collect(Collectors.toSet())));
    }

    /**
     * Является администратором
     *
     * @return boolean
     */
    public boolean isAdmin() {
        return userRoles.contains(UserRoleEnum.ROLE_ADMIN);
    }

    /**
     * Имеет право получать отчеты
     *
     * @return boolean
     */
    public boolean isReportReceiver() {
        return userRoles.contains(UserRoleEnum.ROLE_REPORT_RECEIVER);
    }

    /**
     * Является пользователем
     *
     * @return boolean
     */
    public boolean isUser() {
        return userRoles.contains(UserRoleEnum.ROLE_USER);
    }

    /**
     * Является менеджером хотя бы в одном проекте
     *
     * @return boolean
     */
    public boolean isManager() {
        return userRoles.contains(UserRoleEnum.ROLE_USER)
                && projectIdsByProjectRoles.containsKey(ProjectRoleEnum.MANAGER);
    }

    /**
     * Не является менеджером в заданном проекте
     *
     * @param projectId идентификатор проекта
     * @return boolean
     */
    public boolean isNotManager(int projectId) {
        return userRoles.contains(UserRoleEnum.ROLE_USER)
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
        return userRoles.contains(UserRoleEnum.ROLE_USER)
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
        return userRoles.contains(UserRoleEnum.ROLE_USER)
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
        return userRoles.contains(UserRoleEnum.ROLE_USER)
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
        return userRoles.contains(UserRoleEnum.ROLE_USER)
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
