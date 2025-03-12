package ru.smartup.timetracker.core;

import org.junit.jupiter.api.Test;
import ru.smartup.timetracker.entity.EmployeeProjectRole;
import ru.smartup.timetracker.entity.EmployeeRole;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionEmployeePrincipalTest {
    @Test
    public void testSetAllRoles() {
        List<EmployeeRole> employeeRoles = List.of(
                new EmployeeRole(1, EmployeeRoleEnum.ROLE_ADMIN),
                new EmployeeRole(1, EmployeeRoleEnum.ROLE_REPORT_RECEIVER)
        );
        List<EmployeeProjectRole> employeeProjectRoles = List.of(
                new EmployeeProjectRole(1, 2, ProjectRoleEnum.MANAGER, 1f),
                new EmployeeProjectRole(1, 3, ProjectRoleEnum.EMPLOYEE, 1f),
                new EmployeeProjectRole(1, 4, ProjectRoleEnum.EMPLOYEE, 2f)
        );
        SessionEmployeePrincipal sessionEmployeePrincipal = new SessionEmployeePrincipal(1, "admin");
        sessionEmployeePrincipal.setAllRoles(employeeRoles, employeeProjectRoles);

        Map<ProjectRoleEnum, Set<Integer>> employeeProjectRolesComparable = Map.of(
                ProjectRoleEnum.MANAGER, Set.of(2),
                ProjectRoleEnum.EMPLOYEE, Set.of(3, 4));

        Set<EmployeeRoleEnum> employeeRolesComparable = Set.of(EmployeeRoleEnum.ROLE_ADMIN, EmployeeRoleEnum.ROLE_REPORT_RECEIVER);

        assertTrue((sessionEmployeePrincipal.getProjectIdsByProjectRoles().size() == employeeProjectRolesComparable.size())
                && (sessionEmployeePrincipal.getProjectIdsByProjectRoles().keySet().containsAll(employeeProjectRolesComparable.keySet())
                && employeeProjectRolesComparable.keySet().containsAll(sessionEmployeePrincipal.getProjectIdsByProjectRoles().keySet())));

        assertTrue((sessionEmployeePrincipal.getProjectIdsByProjectRoles().values().containsAll(employeeProjectRolesComparable.values())
                && employeeProjectRolesComparable.values().containsAll(sessionEmployeePrincipal.getProjectIdsByProjectRoles().values())));

        assertTrue((sessionEmployeePrincipal.getEmployeeRoles().size() == employeeRolesComparable.size())
                && (sessionEmployeePrincipal.getEmployeeRoles().containsAll(employeeRolesComparable)
                && employeeRolesComparable.containsAll(sessionEmployeePrincipal.getEmployeeRoles())));
    }
}