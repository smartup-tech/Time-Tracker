package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.smartup.timetracker.entity.TrackedProjectTask;
import ru.smartup.timetracker.entity.field.pk.TrackedProjectTaskPK;
import ru.smartup.timetracker.pojo.TrackedProjectTaskForEmployee;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackedProjectTaskRepository extends JpaRepository<TrackedProjectTask, TrackedProjectTaskPK> {

    @Query("SELECT new ru.smartup.timetracker.pojo.TrackedProjectTaskForEmployee(tp.employeeId, p.id, p.name, t.id, t.name, t.billable) " +
            "FROM TrackedProjectTask tp " +
            "JOIN Task t ON t.id = tp.taskId " +
            "JOIN Project p ON p.id = t.projectId " +
            "WHERE tp.employeeId = :employeeId AND t.isArchived = false AND p.isArchived = false")
    List<TrackedProjectTaskForEmployee> findAllTrackedProjectTaskInfoByEmployeeId(final int employeeId);

    Optional<TrackedProjectTask> findByEmployeeIdAndTaskId(final int employeeId, final long taskId);

    void deleteByTaskId(final long taskId);

    void deleteByEmployeeIdAndTaskId(int employeeId, long taskId);
}
