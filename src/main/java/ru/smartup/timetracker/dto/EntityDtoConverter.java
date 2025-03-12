package ru.smartup.timetracker.dto;

import org.modelmapper.ModelMapper;
import ru.smartup.timetracker.dto.approval.response.*;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitCellDto;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitRowDto;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitTableDayDto;
import ru.smartup.timetracker.dto.tracker.response.TrackUnitTableDto;
import ru.smartup.timetracker.entity.*;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.ProductionCalendarDayEnum;
import ru.smartup.timetracker.entity.Task;
import ru.smartup.timetracker.entity.TrackUnit;
import ru.smartup.timetracker.entity.field.enumerated.TrackUnitStatusEnum;
import ru.smartup.timetracker.pojo.SubmittedWorkDaysForEmployees;
import ru.smartup.timetracker.pojo.TrackedProjectTaskForEmployee;
import ru.smartup.timetracker.utils.DateUtils;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityDtoConverter {
    public static TrackUnitTableDto getTrackUnitTableDto(int employeeId, List<TrackUnit> trackUnitList, List<ProductionCalendarDay> calendarDays, LocalDate firstDayOfWeek,
                                                         ModelMapper modelMapper, FreezeRecord freezeRecord) {
        List<TrackUnitTableDayDto> days = getDayOfWeekInfo(calendarDays, firstDayOfWeek, freezeRecord);

        TrackUnitTableDto trackUnitTableDto = new TrackUnitTableDto();
        trackUnitTableDto.setDays(days);
        trackUnitTableDto.setData(new ArrayList<>());

        Map<Integer, Map<Long, List<TrackUnit>>> mapTrackUnitListByProjectIdAndTaskId = trackUnitList.stream()
                .sorted(Comparator.<TrackUnit, Integer>comparing(trackUnit -> trackUnit.getProject().getId())
                        .thenComparing(trackUnit -> trackUnit.getTask().getId())
                        .thenComparing(TrackUnit::getWorkDay))
                .collect(Collectors.groupingBy(trackUnit -> trackUnit.getProject().getId(),
                        Collectors.groupingBy(trackUnit -> trackUnit.getTask().getId(), Collectors.toList())));

        mapTrackUnitListByProjectIdAndTaskId.forEach((projectId, taskMap) ->
                taskMap.forEach((taskId, trackUnits) -> {
                    TrackUnit firstTrackUnitInList = trackUnits.get(0);
                    trackUnitTableDto.getData().add(getTrackUnitRowDto(employeeId, firstTrackUnitInList.getProject(),
                            firstTrackUnitInList.getTask(), trackUnits, firstDayOfWeek, modelMapper, freezeRecord, false));
                })
        );
        return trackUnitTableDto;
    }

    public static TrackUnitTableDto getTrackUnitTableDto(int employeeId, List<TrackUnit> trackUnitList, List<ProductionCalendarDay> calendarDays, LocalDate firstDayOfWeek,
                                                         ModelMapper modelMapper, FreezeRecord freezeRecord, List<TrackedProjectTaskForEmployee> observedTask) {
        final TrackUnitTableDto trackUnitTableDto = getTrackUnitTableDto(employeeId, trackUnitList, calendarDays, firstDayOfWeek, modelMapper, freezeRecord);

        final Set<Long> taskIds = trackUnitList
                .stream()
                .map(trackUnit -> trackUnit.getTask().getId())
                .collect(Collectors.toSet());

        observedTask.forEach(
                projectTask -> {
                    if (!taskIds.contains(projectTask.getTaskId())) {
                        final TreeMap<String, TrackUnitCellDto> trackUnitCellDtoMap = new TreeMap<>();
                        trackUnitTableDto.getData().add(
                                getTrackUnitRowDtoWithZeroHours(employeeId,
                                        projectTask.getProjectId(), projectTask.getProjectName(),
                                        projectTask.getTaskId(), projectTask.getTaskName(),
                                        firstDayOfWeek, freezeRecord,
                                        true,
                                        projectTask.isBillable(),
                                        trackUnitCellDtoMap)
                        );
                    } else {
                        trackUnitTableDto.getData()
                                .stream()
                                .filter(trackUnitRowDto -> trackUnitRowDto.getTaskId() == projectTask.getTaskId())
                                .findFirst()
                                .ifPresent(
                                        trackUnitRowDto -> trackUnitRowDto.setObserved(true)
                                );
                    }
                }
        );

        return trackUnitTableDto;
    }

    public static TrackUnitRowDto getTrackUnitRowDto(int employeeId, Project project, Task task, List<TrackUnit> trackUnitList,
                                                     LocalDate firstDayOfWeek, ModelMapper modelMapper, FreezeRecord freezeRecord, boolean observed) {
        final TreeMap<String, TrackUnitCellDto> trackUnitCellDtoMap = new TreeMap<>();
        TrackUnitRowDto trackUnitRowDto = getTrackUnitRowDtoWithZeroHours(employeeId, project, task, firstDayOfWeek, freezeRecord, observed, false, trackUnitCellDtoMap);

        trackUnitList.forEach(trackUnit -> {
            TrackUnitCellDto trackUnitCellDto = modelMapper.map(trackUnit, TrackUnitCellDto.class);
            boolean isBlocked = trackUnit.isFrozen() || (!trackUnit.getStatus().equals(TrackUnitStatusEnum.CREATED)
                    && !trackUnit.getStatus().equals(TrackUnitStatusEnum.REJECTED));
            trackUnitCellDto.setBlocked(isBlocked);
            trackUnitCellDto.setRejected(trackUnit.getStatus().equals(TrackUnitStatusEnum.REJECTED));
            trackUnitCellDtoMap.put(trackUnitCellDto.getWorkDay().toString(), trackUnitCellDto);
        });
        trackUnitRowDto.setUnits(new ArrayList<>(trackUnitCellDtoMap.values()));
        return trackUnitRowDto;
    }

    private static TrackUnitRowDto getTrackUnitRowDtoWithZeroHours(final int employeeId, final Project project, final Task task,
                                                                   final LocalDate firstDayOfWeek, final FreezeRecord freezeRecord,
                                                                   final boolean observed, final boolean billable, final TreeMap<String, TrackUnitCellDto> trackUnitCellDtoMap) {
        for (int i = 0; i < DateUtils.DAYS_IN_WEEK; i++) {
            TrackUnitCellDto trackUnitCellDto = new TrackUnitCellDto();
            LocalDate nextDay = firstDayOfWeek.plusDays(i);
            trackUnitCellDto.setWorkDay(Date.valueOf(nextDay));
            boolean isBlocked = (freezeRecord != null) && !nextDay.isAfter(freezeRecord.getFreezeDate());
            trackUnitCellDto.setBlocked(isBlocked);
            trackUnitCellDto.setBillable(billable);
            trackUnitCellDtoMap.put(nextDay.toString(), trackUnitCellDto);
        }

        TrackUnitRowDto trackUnitRowDto = new TrackUnitRowDto();
        trackUnitRowDto.setEmployeeId(employeeId);
        trackUnitRowDto.setProjectId(project.getId());
        trackUnitRowDto.setProjectName(project.getName());
        trackUnitRowDto.setTaskId(task.getId());
        trackUnitRowDto.setTaskName(task.getName());
        trackUnitRowDto.setUnits(new ArrayList<>(trackUnitCellDtoMap.values()));
        trackUnitRowDto.setObserved(observed);

        return trackUnitRowDto;
    }

    private static TrackUnitRowDto getTrackUnitRowDtoWithZeroHours(final int employeeId, final int projectId, final String projectName,
                                                                   final long taskId, final String taskName, final LocalDate firstDayOfWeek, final FreezeRecord freezeRecord,
                                                                   final boolean observed, final boolean billable, final TreeMap<String, TrackUnitCellDto> trackUnitCellDtoMap) {
        return getTrackUnitRowDtoWithZeroHours(
                employeeId,
                new Project(projectId, projectName),
                new Task(taskId, taskName),
                firstDayOfWeek,
                freezeRecord,
                observed,
                billable,
                trackUnitCellDtoMap
        );
    }

    private static List<TrackUnitTableDayDto> getDayOfWeekInfo(final List<ProductionCalendarDay> calendarDays,
                                                               final LocalDate firstDayOfWeek,
                                                               final FreezeRecord freezeRecord) {
        List<TrackUnitTableDayDto> days = new ArrayList<>();

        for (int i = 0; i < DateUtils.DAYS_IN_WEEK; i++) {
            LocalDate nextDay = firstDayOfWeek.plusDays(i);
            boolean isBlocked = (freezeRecord != null) && !nextDay.isAfter(freezeRecord.getFreezeDate());

            final TrackUnitTableDayDto dayDto = new TrackUnitTableDayDto();
            dayDto.setBlocked(isBlocked);
            dayDto.setDate(nextDay);

            setDayInfoForProduction(dayDto,
                    calendarDays
                            .stream()
                            .collect(Collectors.toMap(
                                    productionCalendarDay -> productionCalendarDay.getDay().toLocalDate(),
                                    Function.identity()))
            );

            days.add(dayDto);
        }

        return days;
    }

    private static void setDayInfoForProduction(final MetaDayInfoDto dayInfo, final Map<LocalDate, ProductionCalendarDay> dateToProductionInfo) {
        if (dayInfo.getDate() == null) {
            return;
        }
        final ProductionCalendarDay productionDayInfo = dateToProductionInfo.get(dayInfo.getDate());
        if (productionDayInfo != null) {
            dayInfo.setStatus(productionDayInfo.getStatus());
            dayInfo.setStandardHours(productionDayInfo.getHours());
            return;
        }
        if (dayInfo.getDate().getDayOfWeek() == DayOfWeek.SATURDAY || dayInfo.getDate().getDayOfWeek() == DayOfWeek.SUNDAY) {
            dayInfo.setStatus(ProductionCalendarDayEnum.WEEKEND);
            dayInfo.setStandardHours(0);
            return;
        }
        dayInfo.setStatus(ProductionCalendarDayEnum.WORK_DAY);
        dayInfo.setStandardHours(8);
    }

    public static SubmittedWorkDaysTableDto getSubmittedWorkDaysTableDto(final List<SubmittedWorkDaysForEmployees> submittedHours, final List<ProductionCalendarDay> calendarDays) {
        final List<java.util.Date> dates = submittedHours
                .stream()
                .map(SubmittedWorkDaysForEmployees::getTrackUnitWorkDay)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        if (dates.isEmpty()) {
            return new SubmittedWorkDaysTableDto(new ArrayList<>(), new ArrayList<>());
        }
        final Date minDate = new Date(dates.get(0).getTime());
        final Date maxDate = new Date(dates.get(dates.size() - 1).getTime());

        final SubmittedWorkDaysTableDto submittedWorkDaysTableDto = new SubmittedWorkDaysTableDto();

        submittedWorkDaysTableDto.setDays(getMetaDayInfoBetweenDate(minDate.toLocalDate(), maxDate.toLocalDate(), calendarDays));
        submittedWorkDaysTableDto.setData(getSubmittedWorkDaysTableUnitDto(submittedHours));

        return submittedWorkDaysTableDto;
    }

    private static List<MetaDayInfoDto> getMetaDayInfoBetweenDate(final LocalDate minDate,
                                                                  final LocalDate maxDate,
                                                                  final List<ProductionCalendarDay> calendarDays) {
        if (minDate.isAfter(maxDate)) {
            return new ArrayList<>();
        }

        final Map<LocalDate, ProductionCalendarDay> dateToProductionCalendarDay = calendarDays
                .stream()
                .collect(
                        Collectors.toMap(
                                productionCalendarDay -> productionCalendarDay.getDay().toLocalDate(),
                                Function.identity())
                );

        final List<MetaDayInfoDto> days = new ArrayList<>();
        for (LocalDate tempDate = minDate; tempDate.isBefore(maxDate.plusDays(1)); tempDate = tempDate.plusDays(1)) {
            final MetaDayInfoDto metaDayInfoDto = new MetaDayInfoDto(tempDate);
            days.add(metaDayInfoDto);
            setDayInfoForProduction(metaDayInfoDto, dateToProductionCalendarDay);
        }

        return days;
    }

    private static List<SubmittedWorkDaysTableUnitDto> getSubmittedWorkDaysTableUnitDto(final List<SubmittedWorkDaysForEmployees> submittedHours) {
        final Map<Integer, Map<Long, List<SubmittedWorkDaysForEmployees>>> mapSubmittedWorkDayHoursByEmployeeIdAndTaskId = submittedHours.stream()
                .sorted(Comparator.comparing(SubmittedWorkDaysForEmployees::getEmployeeId)
                        .thenComparing(SubmittedWorkDaysForEmployees::getProjectId)
                        .thenComparing(SubmittedWorkDaysForEmployees::getTaskId)
                        .thenComparing(SubmittedWorkDaysForEmployees::getTrackUnitWorkDay))
                .collect(Collectors.groupingBy(SubmittedWorkDaysForEmployees::getEmployeeId,
                        Collectors.groupingBy(SubmittedWorkDaysForEmployees::getTaskId, Collectors.toList())));

        final List<SubmittedWorkDaysTableUnitDto> results = new ArrayList<>();

        mapSubmittedWorkDayHoursByEmployeeIdAndTaskId.forEach((employeeId, mapSubmittedWorkDayHoursByTaskId) -> {
            final SubmittedWorkDaysTableUnitDto submittedWorkDaysTableUnitDto = new SubmittedWorkDaysTableUnitDto(employeeId);

            final List<SubmittedWorkDaysTableProjectUnitDto> projectUnitDtos = new ArrayList<>();
            final Map<java.util.Date, Float> dateToHours = new TreeMap<>();

            mapSubmittedWorkDayHoursByTaskId.forEach((taskId, submitWorkDays) -> {
                final SubmittedWorkDaysTableProjectUnitDto projectUnitDto = new SubmittedWorkDaysTableProjectUnitDto(taskId);
                final List<SubmittedWorkDayUnitDto> submittedWorkDayUnitDtos = new ArrayList<>();

                submitWorkDays.forEach(submit -> {
                    submittedWorkDaysTableUnitDto.setFirstName(submit.getFirstName());
                    submittedWorkDaysTableUnitDto.setLastName(submit.getLastName());
                    projectUnitDto.setProjectName(submit.getProjectName());
                    projectUnitDto.setProjectId(submit.getProjectId());
                    projectUnitDto.setTaskName(submit.getTaskName());

                    submittedWorkDayUnitDtos.add(new SubmittedWorkDayUnitDto(
                        submit.getTrackUnitId(), submit.getTrackUnitWorkDay(), submit.getTrackUnitHours()
                    ));
                    dateToHours.compute(
                            submit.getTrackUnitWorkDay(),
                            (k, v) -> v == null ? submit.getTrackUnitHours() : v + submit.getTrackUnitHours());
                });

                projectUnitDto.setTrackUnits(submittedWorkDayUnitDtos);
                projectUnitDtos.add(projectUnitDto);
            });

            submittedWorkDaysTableUnitDto.setSummaryTrackUnits(
                    dateToHours
                            .entrySet()
                            .stream()
                            .map(entry -> new SubmittedSummaryWorkDay(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList())
            );

            submittedWorkDaysTableUnitDto.setProjectTrackUnits(projectUnitDtos);

            results.add(submittedWorkDaysTableUnitDto);
        });

        return results;
    }
}
