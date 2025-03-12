package ru.smartup.timetracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.entity.EmployeeProjectRole;
import ru.smartup.timetracker.entity.EmployeeRole;
import ru.smartup.timetracker.repository.EmployeeProjectRoleRepository;
import ru.smartup.timetracker.repository.EmployeeRoleRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис, отвечающий за изменение ролей пользователей, все изменения в базе данных должны
 * отображаться в сессиях пользователей
 */
@RequiredArgsConstructor
@Service
public class RelationEmployeeRolesService {
    private final SessionRegistry sessionRegistry;
    private final EmployeeRoleRepository employeeRoleRepository;
    private final EmployeeProjectRoleRepository employeeProjectRoleRepository;

    /**
     * Мьютексы по идентификаторам пользователей
     * При использовании распределенной системы следует заменить (например, на redis mutex)
     */
    private final ConcurrentHashMap<Integer, Object> mutexEmployeeRolesMap = new ConcurrentHashMap<>();

    public Set<Integer> getManagerIdsByProjectId(int projectId) {
        return employeeProjectRoleRepository.findAllManagerIdByProjectId(projectId);
    }

    public Optional<EmployeeProjectRole> getEmployeeProjectRole(int employee, int projectId) {
        return employeeProjectRoleRepository.findByEmployeeIdAndProjectId(employee, projectId);
    }

    /**
     * Обновить системные роли пользователя
     *
     * @param employeeId    идентификатор пользователя
     * @param employeeRoles роли пользователя
     */
    @Transactional
    public void updateEmployeeRoles(int employeeId, List<EmployeeRole> employeeRoles) {
        synchronized (getMutex(employeeId)) {
            employeeRoleRepository.deleteAllByEmployeeId(employeeId);
            employeeRoleRepository.saveAll(employeeRoles);
            updateRolesInEmployeeSession(employeeId);
        }
    }

    /**
     * Удалить роль пользователя на проекте
     *
     * @param employeeId идентификатор пользователя
     * @param projectId  идентификатор проекта
     */
    @Transactional
    public void deleteEmployeeProjectRole(int employeeId, int projectId) {
        synchronized (getMutex(employeeId)) {
            employeeProjectRoleRepository.deleteByEmployeeIdAndProjectId(employeeId, projectId);
            updateRolesInEmployeeSession(employeeId);
        }
    }

    /**
     * Обновить роль пользователя на проекте
     *
     * @param employeeProjectRole роль пользователя на проекте
     */
    @Transactional
    public void updateEmployeeProjectRole(EmployeeProjectRole employeeProjectRole) {
        synchronized (getMutex(employeeProjectRole.getEmployeeId())) {
            employeeProjectRoleRepository.save(employeeProjectRole);
            updateRolesInEmployeeSession(employeeProjectRole.getEmployeeId());
        }
    }

    /**
     * Установить роли в объект сессий, если он еще не задан.
     * Операция согласована с другими потоками для пользователя с employeeId
     *
     * @param sessionEmployeePrincipal сессионная информация пользователя
     */
    public void setRolesToPrincipalIfNull(SessionEmployeePrincipal sessionEmployeePrincipal) {
        synchronized (getMutex(sessionEmployeePrincipal.getId())) {
            if (sessionEmployeePrincipal.getProjectIdsByProjectRoles() == null) {
                sessionEmployeePrincipal.setAllRoles(employeeRoleRepository.findAllByEmployeeId(sessionEmployeePrincipal.getId()),
                        employeeProjectRoleRepository.findAllByEmployeeId(sessionEmployeePrincipal.getId()));
            }
        }
    }

    /**
     * Обновить роли в сессионных данных пользователя
     *
     * @param employeeId идентификатор пользователя
     */
    private void updateRolesInEmployeeSession(int employeeId) {
        List<EmployeeRole> employeeRoles = employeeRoleRepository.findAllByEmployeeId(employeeId);
        List<EmployeeProjectRole> employeeProjectRoles = employeeProjectRoleRepository.findAllByEmployeeId(employeeId);
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            SessionEmployeePrincipal sessionEmployeePrincipal = (SessionEmployeePrincipal) principal;
            if (sessionEmployeePrincipal.getId() == employeeId) {
                sessionEmployeePrincipal.setAllRoles(employeeRoles, employeeProjectRoles);
                sessionRegistry.getAllSessions(sessionEmployeePrincipal, false)
                        .forEach(sessionInformation -> ((SessionEmployeePrincipal) sessionInformation.getPrincipal())
                                .setAllRoles(employeeRoles, employeeProjectRoles));
                break;
            }
        }
    }

    /**
     * Получить мьютекс для пользователя
     *
     * @param employeeId идентификатор пользователя
     * @return Object
     */
    private Object getMutex(int employeeId) {
        if (mutexEmployeeRolesMap.containsKey(employeeId)) {
            return mutexEmployeeRolesMap.get(employeeId);
        }
        Object mutex = new Object();
        Object usedMutex = mutexEmployeeRolesMap.putIfAbsent(employeeId, mutex);
        return usedMutex == null ? mutex : usedMutex;
    }
}
