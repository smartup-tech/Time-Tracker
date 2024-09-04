package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.field.pk.UserProjectRolePK;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserProjectRoleRepository extends JpaRepository<UserProjectRole, UserProjectRolePK> {
    List<UserProjectRole> findAllByUserId(int userId);

    Optional<UserProjectRole> findByUserIdAndProjectId(int userId, int projectId);

    @Query("SELECT userId FROM UserProjectRole WHERE projectId = :projectId AND projectRoleId = 'MANAGER'")
    Set<Integer> findAllManagerIdByProjectId(int projectId);

    void deleteByUserIdAndProjectId(int userId, int projectId);

    @Modifying
    @Query(value = "DELETE FROM user_project_role upr WHERE upr.user_id = :userId AND upr.project_id IN (SELECT p.id" +
            " FROM project p JOIN user_project_role upr ON upr.project_id = p.id WHERE upr.user_id = :userId AND" +
            " p.is_archived is false)", nativeQuery = true)
    void deleteFromNotArchivedProjectsByUserId(@Param("userId") int userId);
}
