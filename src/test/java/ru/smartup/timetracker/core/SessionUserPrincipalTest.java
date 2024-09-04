package ru.smartup.timetracker.core;

import org.junit.jupiter.api.Test;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionUserPrincipalTest {
    @Test
    public void testSetAllRoles() {
        List<UserRole> userRoles = List.of(
                new UserRole(1, UserRoleEnum.ROLE_ADMIN),
                new UserRole(1, UserRoleEnum.ROLE_REPORT_RECEIVER)
        );
        List<UserProjectRole> userProjectRoles = List.of(
                new UserProjectRole(1, 2, ProjectRoleEnum.MANAGER, 1f),
                new UserProjectRole(1, 3, ProjectRoleEnum.EMPLOYEE, 1f),
                new UserProjectRole(1, 4, ProjectRoleEnum.EMPLOYEE, 2f)
        );
        SessionUserPrincipal sessionUserPrincipal = new SessionUserPrincipal(1, "admin");
        sessionUserPrincipal.setAllRoles(userRoles, userProjectRoles);

        Map<ProjectRoleEnum, Set<Integer>> userProjectRolesComparable = Map.of(
                ProjectRoleEnum.MANAGER, Set.of(2),
                ProjectRoleEnum.EMPLOYEE, Set.of(3, 4));

        Set<UserRoleEnum> userRolesComparable = Set.of(UserRoleEnum.ROLE_ADMIN, UserRoleEnum.ROLE_REPORT_RECEIVER);

        assertTrue((sessionUserPrincipal.getProjectIdsByProjectRoles().size() == userProjectRolesComparable.size())
                && (sessionUserPrincipal.getProjectIdsByProjectRoles().keySet().containsAll(userProjectRolesComparable.keySet())
                && userProjectRolesComparable.keySet().containsAll(sessionUserPrincipal.getProjectIdsByProjectRoles().keySet())));

        assertTrue((sessionUserPrincipal.getProjectIdsByProjectRoles().values().containsAll(userProjectRolesComparable.values())
                && userProjectRolesComparable.values().containsAll(sessionUserPrincipal.getProjectIdsByProjectRoles().values())));

        assertTrue((sessionUserPrincipal.getUserRoles().size() == userRolesComparable.size())
                && (sessionUserPrincipal.getUserRoles().containsAll(userRolesComparable)
                && userRolesComparable.containsAll(sessionUserPrincipal.getUserRoles())));
    }
}