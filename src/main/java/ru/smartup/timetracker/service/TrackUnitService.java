package ru.smartup.timetracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smartup.timetracker.entity.TrackUnit;
import ru.smartup.timetracker.pojo.*;
import ru.smartup.timetracker.repository.TrackUnitBatchRepository;
import ru.smartup.timetracker.repository.TrackUnitRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TrackUnitService {
    private final TrackUnitRepository trackUnitRepository;
    private final TrackUnitBatchRepository trackUnitBatchRepository;

    public boolean hasNoneFinalTrackUnitForUser(int userId) {
        return trackUnitRepository.hasNoneFinalTrackUnitForUser(userId);
    }

    public boolean hasNoneFinalTrackUnitForProject(int projectId) {
        return trackUnitRepository.hasNoneFinalTrackUnitForProject(projectId);
    }

    public boolean hasNoneFinalTrackUnitForTask(long taskId) {
        return trackUnitRepository.hasNoneFinalTrackUnitForTask(taskId);
    }

    public List<TrackUnit> getByUserIdAndRange(int userId, LocalDate firstDayOfWeek, LocalDate lastDayOfWeek) {
        return trackUnitRepository.findAllByUserIdAndRange(userId,
                Date.valueOf(firstDayOfWeek), Date.valueOf(lastDayOfWeek));
    }

    public List<TrackUnit> getByUserIdAndTaskIdAndRange(int userId, long taskId, LocalDate firstDayOfWeek,
                                                        LocalDate lastDayOfWeek) {
        return trackUnitRepository.findAllByUserIdAndTaskIdAndWorkDayBetween(userId, taskId,
                Date.valueOf(firstDayOfWeek), Date.valueOf(lastDayOfWeek));
    }

    public List<TrackUnit> getByUserIdAndProjectIdsAndRange(int userId, Set<Integer> projectIds,
                                                            LocalDate firstDayOfWeek, LocalDate lastDayOfWeek) {
        return trackUnitRepository.findAllByUserIdAndProjectIdsAndRange(userId, projectIds,
                Date.valueOf(firstDayOfWeek), Date.valueOf(lastDayOfWeek));
    }

    public List<TrackUnitUnsubmittedHours> getUnsubmittedHours(int userId) {
        return trackUnitRepository.findUnsubmittedHours(userId).stream()
                .map(weekHours -> new TrackUnitUnsubmittedHours(weekHours.getWeek(), weekHours.getHours()))
                .collect(Collectors.toList());
    }

    public List<TrackUnitUnsubmittedHours> getUnsubmittedHours(int userId, Set<Integer> projectIds) {
        return trackUnitRepository.findUnsubmittedHours(userId, projectIds).stream()
                .map(weekHours -> new TrackUnitUnsubmittedHours(weekHours.getWeek(), weekHours.getHours()))
                .collect(Collectors.toList());
    }

    public List<SubmittedHours> getSubmittedHours() {
        return trackUnitRepository.findSubmittedHours().stream()
                .map(weekHours -> new SubmittedHours(weekHours.getWeek(), weekHours.getHours()))
                .collect(Collectors.toList());
    }

    public List<SubmittedHours> getSubmittedHours(Set<Integer> projectIds) {
        return trackUnitRepository.findSubmittedHours(projectIds).stream()
                .map(weekHours -> new SubmittedHours(weekHours.getWeek(), weekHours.getHours()))
                .collect(Collectors.toList());
    }

    public List<SubmittedHoursByProjects> getSubmittedHoursByProjects(Date week) {
        return trackUnitRepository.findSubmittedHoursByProjects(week).stream()
                .map(hours -> new SubmittedHoursByProjects(hours.getProjectId(), hours.getProjectName(),
                        hours.getSubmittedHours(), hours.getTotalHours()))
                .collect(Collectors.toList());
    }

    public List<SubmittedHoursByProjects> getSubmittedHoursByProjects(Date week, Set<Integer> projectIds) {
        return trackUnitRepository.findSubmittedHoursByProjects(week, projectIds).stream()
                .map(hours -> new SubmittedHoursByProjects(hours.getProjectId(), hours.getProjectName(),
                        hours.getSubmittedHours(), hours.getTotalHours()))
                .collect(Collectors.toList());
    }

    public List<TrackUnit> getSubmittedHoursByWeekAndProjectId(Date week, int projectId) {
        return trackUnitRepository.findAllSubmittedByWeekAndProjectId(week, projectId);
    }

    public List<SubmittedWorkDaysForUsers> getSubmittedHoursForUser(final LocalDate startDate, final LocalDate endDate) {
        return trackUnitRepository.findAllSubmittedHoursForUser(Date.valueOf(startDate), Date.valueOf(endDate));
    }

    public List<SubmittedWorkDaysForUsers> getSubmittedHoursForUser(final Set<Integer> projectIds, final LocalDate startDate, final LocalDate endDate) {
        return trackUnitRepository.findAllSubmittedHoursForUser(projectIds, Date.valueOf(startDate), Date.valueOf(endDate));
    }

    public Set<Integer> getProjectIdsForTrackUnits(List<Long> trackUnitIds) {
        return trackUnitRepository.findProjectIdsForTrackUnits(trackUnitIds);
    }

    public List<TrackUnitProjectNumberUsersHours> getSubmittedHoursAndNumberUsersForProjects() {
        return trackUnitRepository.findSubmittedHoursAndNumberUsersForProjects();
    }

    public List<TrackUnitProjectTask> getTrackUnitsInfo(List<Long> trackUnitIds) {
        return trackUnitRepository.findAllTrackUnitInfo(trackUnitIds);
    }

    @Transactional
    public void insertOrUpdateHoursAndComment(List<TrackUnit> trackUnits, LocalDate freezeDate) {
        List<TrackUnit> trackUnitsAfterFreezeDate = (freezeDate == null) ? trackUnits : trackUnits.stream()
                .filter(trackUnit -> trackUnit.getWorkDay().toLocalDate().isAfter(freezeDate))
                .collect(Collectors.toList());
        if (!trackUnitsAfterFreezeDate.isEmpty()) {
            trackUnitBatchRepository.insertOrUpdateHoursAndComment(trackUnitsAfterFreezeDate);
        }
    }

    @Transactional
    public void deleteTrackUnits(List<TrackUnit> trackUnits) {
        trackUnitBatchRepository.deleteTrackUnits(trackUnits);
    }

    @Transactional
    public int freezeAllByDate(LocalDate date) {
        return trackUnitRepository.freezeAllByDate(Date.valueOf(date));
    }

    @Transactional
    public int unfreezeAllByDate(LocalDate date) {
        return trackUnitRepository.unfreezeAllByDate(Date.valueOf(date));
    }

    @Transactional
    public void submit(int userId, List<Date> weeks) {
        trackUnitRepository.submit(userId, weeks);
    }

    @Transactional
    public void submit(int userId, Set<Integer> projectIds, List<Date> weeks) {
        trackUnitRepository.submit(userId, projectIds, weeks);
    }

    @Transactional
    public void approve(List<Long> trackUnitIds) {
        trackUnitRepository.approve(trackUnitIds);
    }

    @Transactional
    public void reject(List<Long> trackUnitIds, String rejectReason) {
        trackUnitRepository.reject(trackUnitIds, rejectReason);
    }
}
