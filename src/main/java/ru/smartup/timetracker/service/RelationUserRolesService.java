package ru.smartup.timetracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smartup.timetracker.core.SessionUserPrincipal;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.repository.UserProjectRoleRepository;
import ru.smartup.timetracker.repository.UserRoleRepository;

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
public class RelationUserRolesService {
    private final SessionRegistry sessionRegistry;
    private final UserRoleRepository userRoleRepository;
    private final UserProjectRoleRepository userProjectRoleRepository;

    /**
     * Мьютексы по идентификаторам пользователей
     * При использовании распределенной системы следует заменить (например, на redis mutex)
     */
    private final ConcurrentHashMap<Integer, Object> mutexUserRolesMap = new ConcurrentHashMap<>();

    public Set<Integer> getManagerIdsByProjectId(int projectId) {
        return userProjectRoleRepository.findAllManagerIdByProjectId(projectId);
    }

    public Optional<UserProjectRole> getUserProjectRole(int userId, int projectId) {
        return userProjectRoleRepository.findByUserIdAndProjectId(userId, projectId);
    }

    /**
     * Обновить системные роли пользователя
     *
     * @param userId    идентификатор пользователя
     * @param userRoles роли пользователя
     */
    @Transactional
    public void updateUserRoles(int userId, List<UserRole> userRoles) {
        synchronized (getMutex(userId)) {
            userRoleRepository.deleteAllByUserId(userId);
            userRoleRepository.saveAll(userRoles);
            updateRolesInUserSession(userId);
        }
    }

    /**
     * Удалить роль пользователя на проекте
     *
     * @param userId    идентификатор пользователя
     * @param projectId идентификатор проекта
     */
    @Transactional
    public void deleteUserProjectRole(int userId, int projectId) {
        synchronized (getMutex(userId)) {
            userProjectRoleRepository.deleteByUserIdAndProjectId(userId, projectId);
            updateRolesInUserSession(userId);
        }
    }

    /**
     * Обновить роль пользователя на проекте
     *
     * @param userProjectRole роль пользователя на проекте
     */
    @Transactional
    public void updateUserProjectRole(UserProjectRole userProjectRole) {
        synchronized (getMutex(userProjectRole.getUserId())) {
            userProjectRoleRepository.save(userProjectRole);
            updateRolesInUserSession(userProjectRole.getUserId());
        }
    }

    /**
     * Установить роли в объект сессий, если он еще не задан.
     * Операция согласована с другими потоками для пользователя с userId
     *
     * @param sessionUserPrincipal сессионная информация пользователя
     */
    public void setRolesToPrincipalIfNull(SessionUserPrincipal sessionUserPrincipal) {
        synchronized (getMutex(sessionUserPrincipal.getId())) {
            if (sessionUserPrincipal.getProjectIdsByProjectRoles() == null) {
                sessionUserPrincipal.setAllRoles(userRoleRepository.findAllByUserId(sessionUserPrincipal.getId()),
                        userProjectRoleRepository.findAllByUserId(sessionUserPrincipal.getId()));
            }
        }
    }

    /**
     * Обновить роли в сессионных данных пользователя
     *
     * @param userId идентификатор пользователя
     */
    private void updateRolesInUserSession(int userId) {
        List<UserRole> userRoles = userRoleRepository.findAllByUserId(userId);
        List<UserProjectRole> userProjectRoles = userProjectRoleRepository.findAllByUserId(userId);
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            SessionUserPrincipal sessionUserPrincipal = (SessionUserPrincipal) principal;
            if (sessionUserPrincipal.getId() == userId) {
                sessionUserPrincipal.setAllRoles(userRoles, userProjectRoles);
                sessionRegistry.getAllSessions(sessionUserPrincipal, false)
                        .forEach(sessionInformation -> ((SessionUserPrincipal) sessionInformation.getPrincipal())
                                .setAllRoles(userRoles, userProjectRoles));
                break;
            }
        }
    }

    /**
     * Получить мьютекс для пользователя
     *
     * @param userId идентификатор пользователя
     * @return Object
     */
    private Object getMutex(int userId) {
        if (mutexUserRolesMap.containsKey(userId)) {
            return mutexUserRolesMap.get(userId);
        }
        Object mutex = new Object();
        Object usedMutex = mutexUserRolesMap.putIfAbsent(userId, mutex);
        return usedMutex == null ? mutex : usedMutex;
    }
}
