package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.smartup.timetracker.entity.TrackUnit;
import ru.smartup.timetracker.pojo.SubmittedWorkDaysForUsers;
import ru.smartup.timetracker.pojo.TrackUnitProjectNumberUsersHours;
import ru.smartup.timetracker.pojo.TrackUnitProjectTask;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface TrackUnitRepository extends JpaRepository<TrackUnit, Long> {
    @Query(value = "SELECT EXISTS (SELECT 1 FROM track_unit tu WHERE tu.user_id = :userId AND (tu.status = 'CREATED'" +
            " OR tu.status = 'SUBMITTED') AND tu.hours > 0)", nativeQuery = true)
    boolean hasNoneFinalTrackUnitForUser(@Param("userId") int userId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM task t JOIN track_unit tu ON t.id = tu.task_id WHERE" +
            " t.project_id = :projectId AND (tu.status = 'CREATED' OR tu.status = 'SUBMITTED') AND tu.hours > 0)", nativeQuery = true)
    boolean hasNoneFinalTrackUnitForProject(@Param("projectId") int projectId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM track_unit tu WHERE tu.task_id = :taskId AND (tu.status = 'CREATED'" +
            " OR tu.status = 'SUBMITTED') AND tu.hours > 0)", nativeQuery = true)
    boolean hasNoneFinalTrackUnitForTask(@Param("taskId") long taskId);

    @Query("SELECT new TrackUnit(tu.id, p.id, p.name, t.id, t.name, tu.workDay, tu.hours, tu.status, tu.billable, tu.comment, tu.frozen, tu.rejectReason) " +
            "FROM TrackUnit tu JOIN Task t ON t.id = tu.taskId JOIN Project p ON p.id = t.projectId " +
            "WHERE tu.userId = :userId AND tu.workDay BETWEEN :firstDayOfWeek AND :lastDayOfWeek")
    List<TrackUnit> findAllByUserIdAndRange(int userId, Date firstDayOfWeek, Date lastDayOfWeek);

    List<TrackUnit> findAllByUserIdAndTaskIdAndWorkDayBetween(int userId, long taskId, Date firstDayOfWeek, Date lastDayOfWeek);

    @Query("SELECT new TrackUnit(tu.id, p.id, p.name, t.id, t.name, tu.workDay, tu.hours, tu.status, tu.billable, tu.comment, tu.frozen, tu.rejectReason) " +
            "FROM TrackUnit tu JOIN Task t ON t.id = tu.taskId JOIN Project p ON p.id = t.projectId " +
            "WHERE p.id IN :projectIds AND tu.userId = :userId AND tu.workDay BETWEEN :firstDayOfWeek AND :lastDayOfWeek")
    List<TrackUnit> findAllByUserIdAndProjectIdsAndRange(int userId, Set<Integer> projectIds,
                                                         Date firstDayOfWeek, Date lastDayOfWeek);

    @Query(value = "SELECT date_trunc('week', work_day) as week, sum(hours) as hours " +
            "FROM track_unit WHERE status IN ('CREATED', 'REJECTED') AND user_id = :userId AND frozen = false " +
            "GROUP BY date_trunc('week', work_day) HAVING sum(hours) > 0 ORDER BY date_trunc('week', work_day)", nativeQuery = true)
    List<TrackUnitWeekHours> findUnsubmittedHours(int userId);

    @Query(value = "SELECT date_trunc('week', tu.work_day) as week, sum(tu.hours) as hours " +
            "FROM track_unit tu JOIN task t ON t.id = tu.task_id " +
            "WHERE tu.user_id = :userId AND t.project_id IN :projectIds AND tu.status IN ('CREATED', 'REJECTED') AND frozen = false " +
            "GROUP BY date_trunc('week', tu.work_day) HAVING sum(tu.hours) > 0 ORDER BY date_trunc('week', tu.work_day)", nativeQuery = true)
    List<TrackUnitWeekHours> findUnsubmittedHours(int userId, Set<Integer> projectIds);

    @Query(value = "SELECT date_trunc('week', work_day) as week, sum(hours) as hours " +
            "FROM track_unit WHERE status = 'SUBMITTED' AND frozen = false " +
            "GROUP BY date_trunc('week', work_day) HAVING sum(hours) > 0 ORDER BY date_trunc('week', work_day)", nativeQuery = true)
    List<TrackUnitWeekHours> findSubmittedHours();

    @Query(value = "SELECT date_trunc('week', tu.work_day) as week, sum(tu.hours) as hours " +
            "FROM track_unit tu JOIN task t ON t.id = tu.task_id " +
            "WHERE t.project_id IN :projectIds AND tu.status = 'SUBMITTED' AND frozen = false " +
            "GROUP BY date_trunc('week', tu.work_day) HAVING sum(tu.hours) > 0 ORDER BY date_trunc('week', tu.work_day)", nativeQuery = true)
    List<TrackUnitWeekHours> findSubmittedHours(Set<Integer> projectIds);

    @Query(value = "SELECT p.id as projectId, p.\"name\" as projectName, " +
            "sum(CASE WHEN tu.status = 'SUBMITTED' THEN tu.hours ELSE 0 END) as submittedHours, " +
            "sum(tu.hours) as totalHours " +
            "FROM track_unit tu JOIN task t ON t.id = tu.task_id JOIN project p ON p.id = t.project_id " +
            "WHERE date_trunc('week', tu.work_day) = :week AND tu.frozen = false GROUP BY p.id " +
            "HAVING sum(CASE WHEN tu.status = 'SUBMITTED' THEN tu.hours ELSE 0 END) > 0 " +
            "ORDER by p.\"name\"", nativeQuery = true)
    List<TrackUnitByProjectsHours> findSubmittedHoursByProjects(java.sql.Date week);

    @Query(value = "SELECT p.id as projectId, p.\"name\" as projectName, " +
            "sum(CASE WHEN tu.status = 'SUBMITTED' THEN tu.hours ELSE 0 END) as submittedHours, " +
            "sum(tu.hours) as totalHours " +
            "FROM track_unit tu JOIN task t ON t.id = tu.task_id JOIN project p ON p.id = t.project_id " +
            "WHERE date_trunc('week', tu.work_day) = :week AND tu.frozen = false AND p.id IN (:projectIds) GROUP BY p.id " +
            "HAVING sum(CASE WHEN tu.status = 'SUBMITTED' THEN tu.hours ELSE 0 END) > 0 " +
            "ORDER by p.\"name\"", nativeQuery = true)
    List<TrackUnitByProjectsHours> findSubmittedHoursByProjects(java.sql.Date week, Set<Integer> projectIds);

    @Query("SELECT new TrackUnit(tu.id, u.id, u.firstName, u.lastName, t.id, t.name, tu.workDay, tu.hours, tu.status, " +
            "tu.billable, tu.comment) FROM TrackUnit tu JOIN User u ON u.id = tu.userId JOIN Task t " +
            "ON t.id = tu.taskId WHERE tu.status = 'SUBMITTED' AND tu.frozen = false AND date_trunc('week', tu.workDay) = :week " +
            "AND t.projectId = :projectId ORDER BY u.firstName, u.lastName")
    List<TrackUnit> findAllSubmittedByWeekAndProjectId(java.sql.Date week, int projectId);

    @Query("SELECT t.projectId FROM TrackUnit tu JOIN Task t ON t.id = tu.taskId WHERE tu.id IN (:trackUnitIds)")
    Set<Integer> findProjectIdsForTrackUnits(List<Long> trackUnitIds);

    @Modifying
    @Query("UPDATE TrackUnit SET frozen = true WHERE workDay <= :freezeDate AND frozen = false")
    int freezeAllByDate(final Date freezeDate);

    @Modifying
    @Query("UPDATE TrackUnit SET frozen = false WHERE workDay >= :freezeDate AND frozen = true")
    int unfreezeAllByDate(final Date freezeDate);

    @Modifying
    @Query(value = "UPDATE track_unit SET status = 'SUBMITTED' " +
            "WHERE frozen = false AND user_id = :userId AND hours > 0 AND status IN ('CREATED', 'REJECTED') " +
            "AND date_trunc('week', work_day) IN :weeks", nativeQuery = true)
    void submit(int userId, List<java.sql.Date> weeks);

    @Modifying
    @Query(value = "UPDATE track_unit SET status = 'SUBMITTED' FROM task t " +
            "WHERE t.id = task_id AND frozen = false AND user_id = :userId AND hours > 0 AND t.project_id IN :projectIds " +
            "AND status IN ('CREATED', 'REJECTED') AND date_trunc('week', work_day) IN :weeks", nativeQuery = true)
    void submit(int userId, Set<Integer> projectIds, List<java.sql.Date> weeks);

    @Modifying
    @Query("UPDATE TrackUnit SET status = 'APPROVED', rejectReason = null WHERE id IN (:trackUnitIds) " +
            "AND frozen = false AND hours > 0 AND status = 'SUBMITTED'")
    void approve(List<Long> trackUnitIds);

    @Modifying
    @Query("UPDATE TrackUnit SET status = 'REJECTED', rejectReason = :rejectReason WHERE id IN (:trackUnitIds) " +
            "AND frozen = false AND hours > 0 AND status = 'SUBMITTED'")
    void reject(List<Long> trackUnitIds, String rejectReason);

    @Query("SELECT new ru.smartup.timetracker.pojo.TrackUnitProjectNumberUsersHours(t.projectId, COUNT(DISTINCT tu.userId), sum(tu.hours)) " +
            "FROM TrackUnit tu JOIN Task t on t.id = tu.taskId WHERE tu.status = 'SUBMITTED' GROUP BY t.projectId")
    List<TrackUnitProjectNumberUsersHours> findSubmittedHoursAndNumberUsersForProjects();

    @Query("SELECT new ru.smartup.timetracker.pojo.TrackUnitProjectTask(tu.userId, tu.id, tu.workDay, p.id, p.name, t.id, t.name) " +
            "FROM TrackUnit tu JOIN Task t ON t.id = tu.taskId JOIN Project p ON p.id = t.projectId WHERE tu.id IN :trackUnitIds")
    List<TrackUnitProjectTask> findAllTrackUnitInfo(List<Long> trackUnitIds);

    @Query("SELECT new ru.smartup.timetracker.pojo.SubmittedWorkDaysForUsers(" +
            "u.id, u.firstName, u.lastName, tu.workDay, " +
            "p.id, p.name, tu.id, t.id, t.name, tu.hours) " +
            "FROM TrackUnit tu " +
            "JOIN User u on tu.userId = u.id " +
            "JOIN Task t on tu.taskId = t.id " +
            "JOIN Project p ON p.id = t.projectId " +
            "WHERE tu.status = 'SUBMITTED' AND tu.frozen = false AND tu.workDay >= :startDate AND tu.workDay <= :endDate " +
            "GROUP BY (u.id, p.id, tu.id, t.id, tu.workDay, tu.hours) " +
            "ORDER BY tu.workDay, u.id, p.id, t.id")
    List<SubmittedWorkDaysForUsers> findAllSubmittedHoursForUser(final Date startDate, final Date endDate);

    @Query("SELECT new ru.smartup.timetracker.pojo.SubmittedWorkDaysForUsers(" +
            "u.id, u.firstName, u.lastName, tu.workDay, " +
            "p.id, p.name, tu.id, t.id, t.name, tu.hours) " +
            "FROM TrackUnit tu " +
            "JOIN User u on tu.userId = u.id " +
            "JOIN Task t on tu.taskId = t.id " +
            "JOIN Project p ON p.id = t.projectId " +
            "WHERE tu.status = 'SUBMITTED' AND tu.frozen = false AND p.id IN :projectIds AND tu.workDay >= :startDate AND tu.workDay <= :endDate " +
            "GROUP BY (u.id, p.id, tu.id, t.id, tu.workDay, tu.hours) " +
            "ORDER BY tu.workDay, u.id, p.id, t.id")
    List<SubmittedWorkDaysForUsers> findAllSubmittedHoursForUser(final Set<Integer> projectIds, final Date startDate, final Date endDate);

}
