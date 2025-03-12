package ru.smartup.timetracker.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import ru.smartup.timetracker.dto.notice.NoticeCreationDto;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.EmployeeProjectRole;
import ru.smartup.timetracker.entity.EmployeeRole;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.Role;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.pojo.TrackUnitProjectTask;
import ru.smartup.timetracker.pojo.notice.NoticeChanges;
import ru.smartup.timetracker.pojo.notice.NoticeData;
import ru.smartup.timetracker.pojo.notice.NoticeEmployee;
import ru.smartup.timetracker.pojo.notice.NoticeProject;
import ru.smartup.timetracker.pojo.notice.NoticeTrackUnitReject;
import ru.smartup.timetracker.pojo.notice.NoticeUnfreeze;
import ru.smartup.timetracker.service.EmployeeService;
import ru.smartup.timetracker.service.ProjectService;
import ru.smartup.timetracker.service.RelationEmployeeRolesService;
import ru.smartup.timetracker.service.TrackUnitService;
import ru.smartup.timetracker.service.freeze.FreezeTrackUnitAlgorithm;
import ru.smartup.timetracker.service.notification.FreezeTracksSuccessNoticeCreationService;
import ru.smartup.timetracker.service.notification.NoticeScheduleService;
import ru.smartup.timetracker.service.notification.NoticeService;
import ru.smartup.timetracker.service.notification.notifier.NotifierObservable;
import ru.smartup.timetracker.service.notification.strategy.NoticeCreationStrategy;
import ru.smartup.timetracker.utils.CommonUtils;
import ru.smartup.timetracker.utils.DateUtils;
import ru.smartup.timetracker.utils.FreezeDateUtils;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Aspect
@Component
public class NoticeAspect {
    private static final String FIELD_PROJECT_NAME = "projectName";
    private static final String FIELD_PROJECT_ROLE = "projectRole";

    private final RelationEmployeeRolesService relationEmployeeRolesService;
    private final ProjectService projectService;
    private final EmployeeService employeeService;
    private final TrackUnitService trackUnitService;

    private final NotifierObservable notifierObservable;
    private final NoticeScheduleService noticeScheduleService;
    private final FreezeTracksSuccessNoticeCreationService freezeTracksSuccessNoticeCreationService;

    private final FreezeDateUtils freezeDateUtils;

    @Pointcut("execution(* ru.smartup.timetracker.service.ProjectService.updateProject(..)) && args(project)")
    public void callUpdateProject(Project project) {
    }

    @Around(value = "callUpdateProject(project)", argNames = "proceedingJoinPoint, project")
    public Object sendNoticeUpdateProjectToManagers(ProceedingJoinPoint proceedingJoinPoint, Project project) throws Throwable {
        String projectNameBeforeChange = projectService.getProject(project.getId()).map(Project::getName).orElse(null);
        Object result = proceedingJoinPoint.proceed();
        if (project.getName().equals(projectNameBeforeChange)) {
            return result;
        }

        List<Employee> managers = employeeService.getEmployeesByProjectAndProjectRole(project.getId(), ProjectRoleEnum.MANAGER);
        if (managers.isEmpty()) {
            return result;
        }

        final int currentEmployeeId = CommonUtils.getCurrentEmployeeId();

        NoticeData noticeData = new NoticeData(new NoticeProject(project.getId(), project.getName()));
        noticeData.addChange(FIELD_PROJECT_NAME, new NoticeChanges(projectNameBeforeChange, project.getName()));

        Notice notice = new Notice(NoticeTypeEnum.PROJECT_UPDATE, noticeData);
        notice.setText(NoticeService.TEXT_PROJECT_UPDATE);
        notice.setCreatedBy(currentEmployeeId);

        notifierObservable.notifyAllChannels(managers, notice);

        return result;
    }

    @Pointcut("execution(* ru.smartup.timetracker.service.RelationEmployeeRolesService.updateEmployeeProjectRole(..)) && args(employeeProjectRole)")
    public void callUpdateEmployeeProjectRole(EmployeeProjectRole employeeProjectRole) {
    }

    @Around(value = "callUpdateEmployeeProjectRole(employeeProjectRole)", argNames = "proceedingJoinPoint, employeeProjectRole")
    public Object sendNoticeUpdateEmployeeProjectRole(ProceedingJoinPoint proceedingJoinPoint,
                                                      EmployeeProjectRole employeeProjectRole) throws Throwable {
        ProjectRoleEnum projectRoleEnumBeforeChange = relationEmployeeRolesService
                .getEmployeeProjectRole(employeeProjectRole.getEmployeeId(), employeeProjectRole.getProjectId())
                .map(EmployeeProjectRole::getProjectRoleId).orElse(null);

        Object result = proceedingJoinPoint.proceed();

        Optional<Project> existProject = projectService.getProject(employeeProjectRole.getProjectId());

        if (existProject.isEmpty()) {
            return result;
        }

        Project project = existProject.get();
        Notice notice;
        if (projectRoleEnumBeforeChange == null) {

            NoticeData noticeData = new NoticeData(
                    new NoticeProject(project.getId(), project.getName()),
                    new NoticeEmployee(employeeProjectRole.getProjectRoleId())
            );

            notice = new Notice(
                    NoticeTypeEnum.PROJECT_ROLE_GRANTED,
                    employeeProjectRole.getEmployeeId(),
                    NoticeService.TEXT_PROJECT_ROLE_GRANTED,
                    noticeData,
                    CommonUtils.getCurrentEmployeeId());

        } else if (!employeeProjectRole.getProjectRoleId().equals(projectRoleEnumBeforeChange)) {

            NoticeData noticeData = new NoticeData(new NoticeProject(project.getId(), project.getName()));
            noticeData.addChange(FIELD_PROJECT_ROLE, new NoticeChanges(projectRoleEnumBeforeChange,
                    employeeProjectRole.getProjectRoleId()));

            notice = new Notice(
                    NoticeTypeEnum.PROJECT_ROLE_CHANGE,
                    employeeProjectRole.getEmployeeId(),
                    NoticeService.TEXT_PROJECT_ROLE_CHANGE,
                    noticeData,
                    CommonUtils.getCurrentEmployeeId());
        } else {
            notice = null;
        }

        if (notice != null) {
            Optional<Employee> employee = employeeService.getEmployee(employeeProjectRole.getEmployeeId());

            employee.ifPresent((existEmployee) ->
                    notifierObservable.notifyAllChannels(List.of(existEmployee), notice));
        }

        return result;
    }

    @Pointcut(value = "execution(* ru.smartup.timetracker.service.RelationEmployeeRolesService.updateEmployeeRoles(..)) && args(employeeId, employeeRoles)",
            argNames = "employeeId, employeeRoles")
    public void callUpdateEmployeeRoles(int employeeId, List<EmployeeRole> employeeRoles) {
    }

    @Around(value = "callUpdateEmployeeRoles(employeeId, employeeRoles)", argNames = "proceedingJoinPoint, employeeId, employeeRoles")
    public Object sendNoticeUpdateEmployeeRoles(ProceedingJoinPoint proceedingJoinPoint, int employeeId,
                                                List<EmployeeRole> employeeRoles) throws Throwable {
        Set<EmployeeRoleEnum> roles = employeeRoles.stream()
                .map(EmployeeRole::getRoleId)
                .collect(Collectors.toSet());

        Set<EmployeeRoleEnum> rolesBeforeChange = employeeService.getEmployeeRoles(employeeId).stream()
                .map(EmployeeRole::getRoleId)
                .collect(Collectors.toSet());

        Object result = proceedingJoinPoint.proceed();

        if (!rolesBeforeChange.equals(roles)) {
            final int currentEmployeeId = CommonUtils.getCurrentEmployeeId();
            NoticeTypeEnum adminEvent = null;

            if (rolesBeforeChange.contains(EmployeeRoleEnum.ROLE_ADMIN) && !roles.contains(EmployeeRoleEnum.ROLE_ADMIN)) {
                adminEvent = NoticeTypeEnum.ADMIN_REMOVED;
            } else if (!rolesBeforeChange.contains(EmployeeRoleEnum.ROLE_ADMIN) && roles.contains(EmployeeRoleEnum.ROLE_ADMIN)) {
                adminEvent = NoticeTypeEnum.ADMIN_ADDED;
            }

            if (adminEvent != null) {
                NoticeTypeEnum finalAdminEvent = adminEvent;
                employeeService.getEmployee(employeeId).ifPresent(employee -> {
                    String eventText = finalAdminEvent.equals(NoticeTypeEnum.ADMIN_ADDED)
                            ? NoticeService.TEXT_ADMIN_ADDED : NoticeService.TEXT_ADMIN_REMOVED;

                    NoticeData noticeData =
                            new NoticeData(new NoticeEmployee(employee.getId(), employee.getFirstName(), employee.getLastName()));

                    List<Employee> admins = employeeService.getEmployeesByRole(EmployeeRoleEnum.ROLE_ADMIN);

                    final Notice notice = new Notice();
                    notice.setType(finalAdminEvent);
                    notice.setText(eventText);
                    notice.setData(noticeData);
                    notice.setCreatedBy(currentEmployeeId);

                    notifierObservable.notifyAllChannels(admins, notice);
                });
            }
        }

        return result;
    }

    @Pointcut(value = "execution(* ru.smartup.timetracker.service.TrackUnitService.reject(..)) && args(trackUnitIds, ..)",
            argNames = "trackUnitIds")
    public void callRejectTracks(List<Long> trackUnitIds) {
    }

    @After(value = "callRejectTracks(trackUnitIds)", argNames = "trackUnitIds")
    public void sendNoticeRejectTracks(List<Long> trackUnitIds) {
        int currentEmployeeId = CommonUtils.getCurrentEmployeeId();

        Map<Integer, List<TrackUnitProjectTask>> idEmployeesToNoticeData = trackUnitService.getTrackUnitsInfo(trackUnitIds)
                .stream()
                .collect(
                        Collectors.groupingBy(
                                TrackUnitProjectTask::getEmployeeId
                        )
                );

        if (idEmployeesToNoticeData.isEmpty()) {
            return;
        }

        Map<Integer, NoticeTrackUnitReject> employeeIdsToNoticeData = new HashMap<>();

        for (var employeeId : idEmployeesToNoticeData.keySet()) {
            var curTrackUnits = idEmployeesToNoticeData.get(employeeId);

            var minDate = curTrackUnits
                    .stream()
                    .map(TrackUnitProjectTask::getTrackUnitWorkDay)
                    .min(Date::compareTo)
                    .get();

            var maxDate = curTrackUnits
                    .stream()
                    .map(TrackUnitProjectTask::getTrackUnitWorkDay)
                    .max(Date::compareTo)
                    .get();

            employeeIdsToNoticeData.put(employeeId, new NoticeTrackUnitReject(minDate, maxDate));
        }

        List<Employee> employees = employeeService.getEmployees(employeeIdsToNoticeData.keySet());

        for (var employee : employees) {
            Notice notice = new Notice();
            notice.setType(NoticeTypeEnum.HOURS_REJECTED);
            notice.setText(NoticeService.TEXT_HOURS_REJECTED);
            notice.setData(employeeIdsToNoticeData.get(employee.getId()));
            notice.setCreatedBy(currentEmployeeId);

            notifierObservable.notifyAllChannels(List.of(employee), notice);
        }
    }

    @Pointcut(value = "execution(* ru.smartup.timetracker.service.TrackUnitService.freezeAllByDate(..)) && args(date, ..)",
            argNames = "date")
    public void callCompleteFreezeTrackUnits(LocalDate date) {
    }

    @AfterReturning(value = "callCompleteFreezeTrackUnits(date)", argNames = "date")
    public void sendNoticeFreezeTracksSuccess(LocalDate date) {
        LocalDate now = LocalDate.now();
        if (date.isBefore(now)) {
            return;
        }
        final int currentEmployeeId = CommonUtils.getCurrentEmployeeId();

        List<Employee> adminsAndReportReceivers =
                employeeService.getEmployeesByRoles(List.of(EmployeeRoleEnum.ROLE_ADMIN, EmployeeRoleEnum.ROLE_REPORT_RECEIVER));

        Map<EmployeeRoleEnum, List<Employee>> roleListMap = adminsAndReportReceivers.stream()
                .flatMap(employee -> {
                    Set<EmployeeRoleEnum> roles = employee.getEmployeeRoles().stream().map(Role::getRoleId).collect(Collectors.toSet());
                    return roles.stream().map(entry -> Map.entry(entry, employee));
                })
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        NoticeCreationDto noticeCreationDto = NoticeCreationDto.builder()
                .data(new NoticeData(date))
                .createdBy(currentEmployeeId)
                .build();

        roleListMap.forEach((role, employees) -> {
            Optional<NoticeCreationStrategy> strategy = freezeTracksSuccessNoticeCreationService.getStrategy(role);
            if (strategy.isPresent()) {
                Notice notice = strategy.get().createNotice(noticeCreationDto);
                notifierObservable.notifyAllChannels(employees, notice);
            }
        });
    }

    @AfterThrowing(value = "callCompleteFreezeTrackUnits(date)", argNames = "date, throwable", throwing = "throwable")
    public void sendNoticeFreezeTracksError(LocalDate date, Throwable throwable) {
        final int currentEmployeeId = CommonUtils.getCurrentEmployeeId();

        List<Employee> employees = employeeService.getEmployeesByRole(EmployeeRoleEnum.ROLE_ADMIN);

        Notice notice = new Notice();
        notice.setType(NoticeTypeEnum.FREEZE_ERROR);
        notice.setText(NoticeService.TEXT_FREEZE_ERROR);
        notice.setData(new NoticeData(date, throwable.getMessage()));
        notice.setCreatedBy(currentEmployeeId);

        notifierObservable.notifyAllChannels(employees, notice);
    }

    @Pointcut(value = "execution(* ru.smartup.timetracker.service.freeze.FreezeScheduler.scheduleFreeze(..)) && args(freezeRecord, freeze)",
            argNames = "freezeRecord, freeze")
    public void callScheduleFreeze(FreezeRecord freezeRecord, FreezeTrackUnitAlgorithm freeze) {
    }

    @After(value = "callScheduleFreeze(freezeRecord, freeze)", argNames = "freezeRecord, freeze")
    public void sendNoticeFutureFreeze(FreezeRecord freezeRecord, FreezeTrackUnitAlgorithm freeze) {
        LocalDate now = LocalDate.now();
        LocalDate freezeDate = freezeRecord.getFreezeDate();
        if (freezeDate.isAfter(now) || freezeDate.isEqual(now)) {
            noticeScheduleService.scheduleFreezeNotice(freezeRecord);
        }
    }

    @Pointcut(value = "execution(* ru.smartup.timetracker.service.freeze.FreezeScheduler.cancelFreezeTask())")
    public void callCancelFreezeTrackUnits() {
    }

    @AfterReturning(value = "callCancelFreezeTrackUnits()")
    public void cancelFreezeTrackUnits() {
        noticeScheduleService.cancelNotice();
    }

    @Pointcut(value = "execution(* ru.smartup.timetracker.service.freeze.FreezeScheduler.unfreeze(..)) && args(unfreezeRecord, ..)", argNames = "unfreezeRecord")
    public void callUnFreezeTrackUnits(FreezeRecord unfreezeRecord) {
    }

    @After(value = "callUnFreezeTrackUnits(unfreezeRecord)", argNames = "unfreezeRecord")
    public void notifyUnfreezeRecords(FreezeRecord unfreezeRecord) {
        List<Employee> admins = employeeService.getEmployeesByRole(EmployeeRoleEnum.ROLE_ADMIN);

        String time = DateUtils.formatZoneDate(freezeDateUtils.getZoneUnfreezingTimestamp());

        Notice notice = new Notice();
        notice.setData(
                new NoticeUnfreeze(unfreezeRecord.getFreezeDate(), time)
        );
        notice.setText(NoticeService.TEXT_UN_FREEZE);
        notice.setType(NoticeTypeEnum.UN_FREEZE);
        notice.setCreatedBy(admins.get(0).getId());

        notifierObservable.notifyAllChannels(admins, notice);
    }
}
