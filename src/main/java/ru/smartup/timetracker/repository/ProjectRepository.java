package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.smartup.timetracker.entity.Project;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {
    List<Project> findAllByIdIn(Set<Integer> projectIds);

    Optional<Project> findByIdAndIsArchivedFalse(int projectId);

    @Query("SELECT p FROM Project p JOIN UserProjectRole upr ON upr.projectId = p.id WHERE upr.userId = :userId" +
            " AND p.isArchived = false")
    List<Project> findAllNotArchivedProjectsOfUser(@Param("userId") int userId);

    @Query("SELECT p.id as id, p.name as name, p.isArchived as archived, upr.externalRate as externalRate, " +
            "upr.projectRoleId as projectRoleId FROM Project p " +
            "JOIN UserProjectRole upr ON upr.projectId = p.id WHERE upr.userId = :userId AND p.isArchived = false")
    List<ProjectWithRole> findAllNotArchivedProjectsOfUserWithRole(@Param("userId") int userId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM project WHERE name = :projectName)", nativeQuery = true)
    boolean isNotUnique(@Param("projectName") String projectName);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM project WHERE id <> :projectId AND name = :projectName)",
            nativeQuery = true)
    boolean isNotUnique(@Param("projectId") int projectId, @Param("projectName") String projectName);

    @Modifying
    @Query("UPDATE Project SET isArchived = true WHERE id = :projectId")
    void archiveProject(@Param("projectId") int projectId);

    List<Project> findAllByIsArchivedFalseOrderByName();
}
