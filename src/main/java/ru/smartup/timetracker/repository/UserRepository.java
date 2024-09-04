package ru.smartup.timetracker.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmailAndIsArchivedFalse(String email);

    Optional<User> findByIdAndIsArchivedFalse(int userId);

    Optional<User> findByIdAndIsArchivedTrue(int userId);

    List<User> findAllByIsArchivedFalse();

    List<User> findByPositionIdAndIsArchivedFalse(@Param("positionId") int positionId);

    @Query("SELECT DISTINCT u FROM User u JOIN UserRole ur ON ur.userId = u.id" +
            " WHERE u.isArchived = false AND (UPPER(u.firstName) LIKE UPPER(CONCAT('%', :searchValue, '%'))" +
            " OR UPPER(u.lastName) LIKE UPPER(CONCAT('%', :searchValue, '%')))" +
            " AND ur.roleId IN ('ROLE_ADMIN', 'ROLE_USER')" +
            " AND u.id NOT IN (SELECT upr.userId from UserProjectRole upr WHERE upr.projectId = :projectId)")
    List<User> findCandidatesForProject(@Param("projectId") int projectId,
                                        @Param("searchValue") String searchValue,
                                        Pageable pageable);

    @Query("SELECT new User(upr.projectId, upr.userId, u.firstName, u.middleName, u.lastName, upr.projectRoleId," +
            " upr.externalRate) FROM User u JOIN UserProjectRole upr ON upr.userId = u.id WHERE" +
            " upr.projectId = :projectId")
    List<User> findAllUsersInProject(@Param("projectId") int projectId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM \"user\" WHERE email = :email)", nativeQuery = true)
    boolean isNotUnique(@Param("email") String email);

    @Modifying
    @Query("UPDATE User SET isArchived = :archived WHERE id = :userId")
    void updateArchiveStatus(@Param("userId") int userId, @Param("archived") final boolean archived);

    @Modifying
    @Query("UPDATE User SET passwordHash = :newPasswordHash WHERE id = :userId AND passwordHash = :oldPasswordHash")
    void updatePassword(int userId, String oldPasswordHash, String newPasswordHash);

    @Query(value = "UPDATE \"user\" SET password_hash = :passwordHash WHERE id = :userId",
            nativeQuery = true)
    Optional<User> updatePassword(int userId, String passwordHash);

    List<User> findAllByFirstNameContainingOrLastNameContainingAllIgnoreCase(String partOfFirstName,
                                                                             String partOfLastName,
                                                                             Sort sort);

    @Query("SELECT DISTINCT u FROM User u JOIN UserProjectRole upr ON upr.userId = u.id " +
            "WHERE upr.projectId IN :projectIds AND u.isArchived = :archive AND (UPPER(u.firstName) LIKE UPPER(CONCAT('%', :searchValue, '%')) " +
            "OR UPPER(u.lastName) LIKE UPPER(CONCAT('%', :searchValue, '%')))")
    List<User> findAllInProjectsByFirstNameOrLastNameAndArchive(Set<Integer> projectIds, String searchValue, boolean archive, Sort sort);


    @Query("SELECT DISTINCT u FROM User u JOIN UserProjectRole upr on upr.userId = u.id " +
            "WHERE upr.projectId = :projectId AND upr.projectRoleId = :userProjectRole")
    List<User> findAllByProjectIdAndProjectRole(final int projectId, final ProjectRoleEnum userProjectRole);

    @Query("SELECT DISTINCT u FROM User u JOIN UserRole ur ON ur.userId = u.id " +
            "WHERE ur.roleId = :userRole")
    List<User> findAllByUserRole(final UserRoleEnum userRole);

    @Query("SELECT DISTINCT u FROM User u JOIN UserRole ur ON ur.userId = u.id " +
            "WHERE ur.roleId in :userRoles")
    List<User> findAllByUserRoles(List<UserRoleEnum> userRoles);
}
