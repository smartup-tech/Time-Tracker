package ru.smartup.timetracker.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EmployeeRepository extends JpaRepository<Employee, Integer>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByEmailAndIsArchivedFalse(String email);

    Optional<Employee> findByIdAndIsArchivedFalse(int employeeId);

    Optional<Employee> findByIdAndIsArchivedTrue(int employeeId);

    List<Employee> findAllByIsArchivedFalse();

    List<Employee> findByPositionIdAndIsArchivedFalse(@Param("positionId") int positionId);

    @Query("SELECT DISTINCT e FROM Employee e JOIN EmployeeRole er ON er.employeeId = e.id" +
            " WHERE e.isArchived = false AND (UPPER(e.firstName) LIKE UPPER(CONCAT('%', :searchValue, '%'))" +
            " OR UPPER(e.lastName) LIKE UPPER(CONCAT('%', :searchValue, '%')))" +
            " AND er.roleId IN ('ROLE_ADMIN', 'ROLE_EMPLOYEE')" +
            " AND e.id NOT IN (SELECT epr.employeeId from EmployeeProjectRole epr WHERE epr.projectId = :projectId)")
    List<Employee> findCandidatesForProject(@Param("projectId") int projectId,
                                            @Param("searchValue") String searchValue,
                                            Pageable pageable);

    @Query("SELECT new Employee(epr.projectId, epr.employeeId, e.firstName, e.middleName, e.lastName, epr.projectRoleId," +
            " epr.externalRate) FROM Employee e JOIN EmployeeProjectRole epr ON epr.employeeId = e.id WHERE" +
            " epr.projectId = :projectId")
    List<Employee> findAllEmployeesInProject(@Param("projectId") int projectId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM employee WHERE email = :email)", nativeQuery = true)
    boolean isNotUnique(@Param("email") String email);

    @Modifying
    @Query("UPDATE Employee SET isArchived = :archived WHERE id = :employeeId")
    void updateArchiveStatus(@Param("employeeId") int employeeId, @Param("archived") final boolean archived);

    @Modifying
    @Query("UPDATE Employee SET passwordHash = :newPasswordHash WHERE id = :employeeId AND passwordHash = :oldPasswordHash")
    void updatePassword(int employeeId, String oldPasswordHash, String newPasswordHash);

    @Query(value = "UPDATE Employee SET password_hash = :passwordHash WHERE id = :employeeId",
            nativeQuery = true)
    Optional<Employee> updatePassword(int employeeId, String passwordHash);

    List<Employee> findAllByFirstNameContainingOrLastNameContainingAllIgnoreCase(String partOfFirstName,
                                                                                 String partOfLastName,
                                                                                 Sort sort);

    @Query("SELECT DISTINCT e FROM Employee e JOIN EmployeeProjectRole epr ON epr.employeeId = e.id " +
            "WHERE epr.projectId IN :projectIds AND e.isArchived = :archive AND (UPPER(e.firstName) LIKE UPPER(CONCAT('%', :searchValue, '%')) " +
            "OR UPPER(e.lastName) LIKE UPPER(CONCAT('%', :searchValue, '%')))")
    List<Employee> findAllInProjectsByFirstNameOrLastNameAndArchive(Set<Integer> projectIds, String searchValue, boolean archive, Sort sort);


    @Query("SELECT DISTINCT e FROM Employee e JOIN EmployeeProjectRole epr on epr.employeeId = e.id " +
            "WHERE epr.projectId = :projectId AND epr.projectRoleId = :employeeProjectRole")
    List<Employee> findAllByProjectIdAndProjectRole(final int projectId, final ProjectRoleEnum employeeProjectRole);

    @Query("SELECT DISTINCT e FROM Employee e JOIN EmployeeRole er ON er.employeeId = e.id " +
            "WHERE er.roleId = :employeeRole")
    List<Employee> findAllByEmployeeRole(final EmployeeRoleEnum employeeRole);

    @Query("SELECT DISTINCT e FROM Employee e JOIN EmployeeRole er ON er.employeeId = e.id " +
            "WHERE er.roleId in :employeeRoles")
    List<Employee> findAllByEmployeeRoles(List<EmployeeRoleEnum> employeeRoles);
}
