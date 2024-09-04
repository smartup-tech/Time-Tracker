package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.smartup.timetracker.entity.TrackedProjectTask;
import ru.smartup.timetracker.entity.field.pk.TrackedProjectTaskPK;
import ru.smartup.timetracker.pojo.TrackedProjectTaskForUser;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackedProjectTaskRepository extends JpaRepository<TrackedProjectTask, TrackedProjectTaskPK> {

    @Query("SELECT new ru.smartup.timetracker.pojo.TrackedProjectTaskForUser(tp.userId, p.id, p.name, t.id, t.name, t.billable) " +
            "FROM TrackedProjectTask tp " +
            "JOIN Task t ON t.id = tp.taskId " +
            "JOIN Project p ON p.id = t.projectId " +
            "WHERE tp.userId = :userId AND t.isArchived = false AND p.isArchived = false")
    List<TrackedProjectTaskForUser> findAllTrackedProjectTaskInfoByUserId(final int userId);

    Optional<TrackedProjectTask> findByUserIdAndTaskId(final int userId, final long taskId);

    void deleteByTaskId(final long taskId);

    void deleteByUserIdAndTaskId(int userId, long taskId);
}
