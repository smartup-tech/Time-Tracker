package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.smartup.timetracker.entity.EmployeeProjectRole;
import ru.smartup.timetracker.entity.field.pk.EmployeeProjectRolePK;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EmployeeProjectRoleRepository extends JpaRepository<EmployeeProjectRole, EmployeeProjectRolePK> {
    List<EmployeeProjectRole> findAllByEmployeeId(int employeeId);

    Optional<EmployeeProjectRole> findByEmployeeIdAndProjectId(int employeeId, int projectId);

    @Query("SELECT employeeId FROM EmployeeProjectRole WHERE projectId = :projectId AND projectRoleId = 'MANAGER'")
    Set<Integer> findAllManagerIdByProjectId(int projectId);

    void deleteByEmployeeIdAndProjectId(int employeeId, int projectId);

    @Modifying
    @Query(value = "DELETE FROM employee_project_role epr WHERE epr.employee_id = :employeeId AND epr.project_id IN (SELECT p.id" +
            " FROM project p JOIN employee_project_role epr ON epr.project_id = p.id WHERE epr.employee_id = :employeeId AND" +
            " p.is_archived is false)", nativeQuery = true)
    void deleteFromNotArchivedProjectsByEmployeeId(@Param("employeeId") int employeeId);
}
