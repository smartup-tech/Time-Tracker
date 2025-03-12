package ru.smartup.timetracker.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.smartup.timetracker.pojo.ReportHours;
import ru.smartup.timetracker.repository.ReportRepository;
import ru.smartup.timetracker.repository.mapper.ReportHoursForProjectsMapper;
import ru.smartup.timetracker.repository.mapper.ReportHoursForEmployeesMapper;

import java.sql.Date;
import java.sql.Types;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepository {
    private static final String QUERY_REPORT_HOURS_FOR_PROJECTS = "SELECT p.id project_id, p.name project_name,\n" +
            "       sum(CASE WHEN tu.billable = true THEN tu.hours ELSE 0 END) billable_hours,\n" +
            "       sum(CASE WHEN tu.billable = true AND tu.frozen = true THEN tu.hours ELSE 0 END) billable_hours_frozen,\n" +
            "       sum(CASE WHEN tu.billable = true AND tu.frozen <> true THEN tu.hours ELSE 0 END) billable_hours_not_frozen,\n" +
            "       sum(CASE WHEN tu.billable <> true THEN tu.hours ELSE 0 END) unbillable_hours,\n" +
            "       sum(CASE WHEN tu.billable <> true AND tu.frozen = true THEN tu.hours ELSE 0 END) unbillable_hours_frozen,\n" +
            "       sum(CASE WHEN tu.billable <> true AND tu.frozen <> true THEN tu.hours ELSE 0 END) unbillable_hours_not_frozen,\n" +
            "       sum(tu.hours) total_hours,\n" +
            "       sum(CASE WHEN tu.frozen = true THEN tu.hours ELSE 0 END) total_hours_frozen,\n" +
            "       sum(CASE WHEN tu.frozen <> true THEN tu.hours ELSE 0 END) total_hours_not_frozen\n" +
            "FROM track_unit tu\n" +
            "         INNER JOIN task t ON t.id = tu.task_id\n" +
            "         INNER JOIN project p ON p.id = t.project_id\n" +
            "WHERE (:projectIdsEmpty OR p.id IN (:projectIds)) AND tu.hours > 0 AND tu.work_day BETWEEN :startDate AND :endDate\n" +
            "GROUP BY p.id";

    private static final String QUERY_REPORT_HOURS_FOR_EMPLOYEES = "SELECT p.id project_id, p.name project_name,\n" +
            "       e.id employee_id, e.first_name employee_first_name, e.last_name employee_last_name,\n" +
            "       t.id task_id, t.name task_name, json_object_agg(tu.work_day, tu.hours) AS work_hours_map," +
            "       sum(CASE WHEN tu.billable = true THEN tu.hours ELSE 0 END) billable_hours,\n" +
            "       sum(CASE WHEN tu.billable = true AND tu.frozen = true THEN tu.hours ELSE 0 END) billable_hours_frozen,\n" +
            "       sum(CASE WHEN tu.billable = true AND tu.frozen <> true THEN tu.hours ELSE 0 END) billable_hours_not_frozen,\n" +
            "       sum(CASE WHEN tu.billable <> true THEN tu.hours ELSE 0 END) unbillable_hours,\n" +
            "       sum(CASE WHEN tu.billable <> true AND tu.frozen = true THEN tu.hours ELSE 0 END) unbillable_hours_frozen,\n" +
            "       sum(CASE WHEN tu.billable <> true AND tu.frozen <> true THEN tu.hours ELSE 0 END) unbillable_hours_not_frozen,\n" +
            "       sum(tu.hours) total_hours,\n" +
            "       sum(CASE WHEN tu.frozen = true THEN tu.hours ELSE 0 END) total_hours_frozen,\n" +
            "       sum(CASE WHEN tu.frozen <> true THEN tu.hours ELSE 0 END) total_hours_not_frozen\n" +
            "FROM track_unit tu\n" +
            "         INNER JOIN employee e ON e.id = tu.employee_id\n" +
            "         INNER JOIN task t ON t.id = tu.task_id\n" +
            "         INNER JOIN project p ON p.id = t.project_id\n" +
            "WHERE (:projectIdsEmpty OR p.id IN (:projectIds)) AND tu.hours > 0 AND tu.work_day BETWEEN :startDate AND :endDate\n" +
            "GROUP BY p.id, e.id, t.id";

    private static final String QUERY_REPORT_HOURS_FOR_CURRENT_EMPLOYEE = "SELECT p.id project_id, p.name project_name,\n" +
            "       e.id employee_id, e.first_name employee_first_name, e.last_name employee_last_name,\n" +
            "       t.id task_id, t.name task_name, json_object_agg(tu.work_day, tu.hours) AS work_hours_map," +
            "       sum(CASE WHEN tu.billable = true THEN tu.hours ELSE 0 END) billable_hours,\n" +
            "       sum(CASE WHEN tu.billable = true AND tu.frozen = true THEN tu.hours ELSE 0 END) billable_hours_frozen,\n" +
            "       sum(CASE WHEN tu.billable = true AND tu.frozen <> true THEN tu.hours ELSE 0 END) billable_hours_not_frozen,\n" +
            "       sum(CASE WHEN tu.billable <> true THEN tu.hours ELSE 0 END) unbillable_hours,\n" +
            "       sum(CASE WHEN tu.billable <> true AND tu.frozen = true THEN tu.hours ELSE 0 END) unbillable_hours_frozen,\n" +
            "       sum(CASE WHEN tu.billable <> true AND tu.frozen <> true THEN tu.hours ELSE 0 END) unbillable_hours_not_frozen,\n" +
            "       sum(tu.hours) total_hours,\n" +
            "       sum(CASE WHEN tu.frozen = true THEN tu.hours ELSE 0 END) total_hours_frozen,\n" +
            "       sum(CASE WHEN tu.frozen <> true THEN tu.hours ELSE 0 END) total_hours_not_frozen\n" +
            "FROM track_unit tu\n" +
            "         INNER JOIN employee e ON e.id = tu.employee_id\n" +
            "         INNER JOIN task t ON t.id = tu.task_id\n" +
            "         INNER JOIN project p ON p.id = t.project_id\n" +
            "WHERE tu.employee_id = :employeeId AND tu.hours > 0 AND tu.work_day BETWEEN :startDate AND :endDate\n" +
            "GROUP BY p.id, e.id, t.id";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * projectIds - пустой, выводим время для всех проектов
     * projectIds - не пустой, выводим время для указанных проектов
     *
     * @param projectIds идентификаторы проектов
     * @param startDate  начало периода
     * @param endDate    конец периода
     * @return List<ReportHours>
     */
    @Override
    public List<ReportHours> getReportHoursForProjects(Set<Integer> projectIds, Date startDate, Date endDate) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("projectIdsEmpty", projectIds.isEmpty(), Types.BOOLEAN);
        parameters.addValue("projectIds", projectIds.isEmpty() ? null : projectIds, Types.INTEGER);
        parameters.addValue("startDate", startDate, Types.DATE);
        parameters.addValue("endDate", endDate, Types.DATE);
        return namedParameterJdbcTemplate.query(QUERY_REPORT_HOURS_FOR_PROJECTS, parameters, new ReportHoursForProjectsMapper());
    }

    /**
     * projectIds - пустой, выводим пользователей со всех проектов
     * projectIds - не пустой, выводим всех пользователей из указанных проектов
     *
     * @param projectIds идентификаторы проектов
     * @param startDate  начало периода
     * @param endDate    конец периода
     * @return List<ReportHours>
     */
    @Override
    public List<ReportHours> getReportHoursForEmployees(Set<Integer> projectIds, Date startDate, Date endDate) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("projectIdsEmpty", projectIds.isEmpty(), Types.BOOLEAN);
        parameters.addValue("projectIds", projectIds.isEmpty() ? null : projectIds, Types.INTEGER);
        parameters.addValue("startDate", startDate, Types.DATE);
        parameters.addValue("endDate", endDate, Types.DATE);
        return namedParameterJdbcTemplate.query(QUERY_REPORT_HOURS_FOR_EMPLOYEES, parameters, new ReportHoursForEmployeesMapper());
    }

    /**
     * Получить отчет для текущего пользователя за указанный период
     *
     * @param employeeId    идентификатор текущего пользователя
     * @param startDate начало периода
     * @param endDate   конец периода
     * @return List<ReportHours>
     */
    @Override
    public List<ReportHours> getReportHoursForCurrentEmployee(int employeeId, Date startDate, Date endDate) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("employeeId", employeeId, Types.INTEGER);
        parameters.addValue("startDate", startDate, Types.DATE);
        parameters.addValue("endDate", endDate, Types.DATE);
        return namedParameterJdbcTemplate.query(QUERY_REPORT_HOURS_FOR_CURRENT_EMPLOYEE, parameters, new ReportHoursForEmployeesMapper());
    }
}
