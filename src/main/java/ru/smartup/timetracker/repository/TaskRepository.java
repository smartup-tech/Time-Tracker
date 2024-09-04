package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.smartup.timetracker.entity.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByProjectId(int projectId);

    Optional<Task> findByIdAndIsArchivedFalse(long taskId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM task WHERE project_id = :projectId AND name = :taskName)",
            nativeQuery = true)
    boolean isNotUnique(@Param("projectId") int projectId, @Param("taskName") String taskName);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM task WHERE project_id = :projectId AND id <> :taskId AND" +
            " name = :taskName)", nativeQuery = true)
    boolean isNotUnique(@Param("projectId") int projectId,
                        @Param("taskId") long taskId,
                        @Param("taskName") String taskName);

    @Modifying
    @Query("UPDATE Task SET isArchived = true WHERE id = :taskId")
    void archiveTask(@Param("taskId") long taskId);

    @Modifying
    @Query("UPDATE Task SET isArchived = true WHERE projectId = :projectId")
    void archiveAllTasksFromProject(@Param("projectId") int projectId);
}
