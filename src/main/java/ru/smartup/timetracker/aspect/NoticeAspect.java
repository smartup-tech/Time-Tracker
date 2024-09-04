package ru.smartup.timetracker.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.pojo.TrackUnitProjectTask;
import ru.smartup.timetracker.pojo.notice.*;
import ru.smartup.timetracker.service.ProjectService;
import ru.smartup.timetracker.service.RelationUserRolesService;
import ru.smartup.timetracker.service.TrackUnitService;
import ru.smartup.timetracker.service.UserService;
import ru.smartup.timetracker.service.freeze.CRUDFreezeService;
import ru.smartup.timetracker.service.notification.NoticeScheduleService;
import ru.smartup.timetracker.service.notification.NoticeService;
import ru.smartup.timetracker.service.notification.notifier.NotifierObservable;
import ru.smartup.timetracker.utils.CommonUtils;
import ru.smartup.timetracker.utils.DateUtils;
import ru.smartup.timetracker.utils.FreezeDateUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Aspect
@Component
public class NoticeAspect {
    private static final String FIELD_PROJECT_NAME = "projectName";
    private static final String FIELD_PROJECT_ROLE = "projectRole";

    private final RelationUserRolesService relationUserRolesService;
    private final ProjectService projectService;
    private final UserService userService;
    private final TrackUnitService trackUnitService;

    private final NotifierObservable notifierObservable;
    private final NoticeScheduleService noticeScheduleService;

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

        List<User> managers = userService.getUsersByProjectAndProjectRole(project.getId(), ProjectRoleEnum.MANAGER);
        if (managers.isEmpty()) {
            return result;
        }

        final int currentUserId = CommonUtils.getCurrentUserId();

        NoticeData noticeData = new NoticeData(new NoticeProject(project.getId(), project.getName()));
        noticeData.addChange(FIELD_PROJECT_NAME, new NoticeChanges(projectNameBeforeChange, project.getName()));

        Notice notice = new Notice(NoticeTypeEnum.PROJECT_UPDATE, noticeData);
        notice.setText(NoticeService.TEXT_PROJECT_UPDATE);
        notice.setCreatedBy(currentUserId);

        notifierObservable.notifyAllChannels(managers, notice);

        return result;
    }

    @Pointcut("execution(* ru.smartup.timetracker.service.RelationUserRolesService.updateUserProjectRole(..)) && args(userProjectRole)")
    public void callUpdateUserProjectRole(UserProjectRole userProjectRole) {
    }

    @Around(value = "callUpdateUserProjectRole(userProjectRole)", argNames = "proceedingJoinPoint, userProjectRole")
    public Object sendNoticeUpdateUserProjectRole(ProceedingJoinPoint proceedingJoinPoint,
                                                  UserProjectRole userProjectRole) throws Throwable {
        ProjectRoleEnum projectRoleEnumBeforeChange = relationUserRolesService
                .getUserProjectRole(userProjectRole.getUserId(), userProjectRole.getProjectId())
                .map(UserProjectRole::getProjectRoleId).orElse(null);

        Object result = proceedingJoinPoint.proceed();

        Optional<Project> existProject = projectService.getProject(userProjectRole.getProjectId());

        if (existProject.isEmpty()) {
            return result;
        }

        Project project = existProject.get();
        Notice notice;
        if (projectRoleEnumBeforeChange == null) {

            NoticeData noticeData = new NoticeData(
                    new NoticeProject(project.getId(), project.getName()),
                    new NoticeUser(userProjectRole.getProjectRoleId())
            );

            notice = new Notice(
                    NoticeTypeEnum.PROJECT_ROLE_GRANTED,
                    userProjectRole.getUserId(),
                    NoticeService.TEXT_PROJECT_ROLE_GRANTED,
                    noticeData,
                    CommonUtils.getCurrentUserId());

        } else if (!userProjectRole.getProjectRoleId().equals(projectRoleEnumBeforeChange)) {

            NoticeData noticeData = new NoticeData(new NoticeProject(project.getId(), project.getName()));
            noticeData.addChange(FIELD_PROJECT_ROLE, new NoticeChanges(projectRoleEnumBeforeChange,
                    userProjectRole.getProjectRoleId()));

            notice = new Notice(
                    NoticeTypeEnum.PROJECT_ROLE_CHANGE,
                    userProjectRole.getUserId(),
                    NoticeService.TEXT_PROJECT_ROLE_CHANGE,
                    noticeData,
                    CommonUtils.getCurrentUserId());
        } else {
            notice = null;
        }

        if (notice != null) {
            Optional<User> user = userService.getUser(userProjectRole.getUserId());

            user.ifPresent((existUser) ->
                    notifierObservable.notifyAllChannels(List.of(existUser), notice));
        }

        return result;
    }

    @Pointcut(value = "execution(* ru.smartup.timetracker.service.RelationUserRolesService.updateUserRoles(..)) && args(userId, userRoles)",
            argNames = "userId, userRoles")
    public void callUpdateUserRoles(int userId, List<UserRole> userRoles) {
    }

    @Around(value = "callUpdateUserRoles(userId, userRoles)", argNames = "proceedingJoinPoint, userId, userRoles")
    public Object sendNoticeUpdateUserRoles(ProceedingJoinPoint proceedingJoinPoint, int userId,
                                            List<UserRole> userRoles) throws Throwable {
        Set<UserRoleEnum> roles = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toSet());

        Set<UserRoleEnum> rolesBeforeChange = userService.getUserRoles(userId).stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toSet());

        Object result = proceedingJoinPoint.proceed();

        if (!rolesBeforeChange.equals(roles)) {
            final int currentUserId = CommonUtils.getCurrentUserId();
            NoticeTypeEnum adminEvent = null;

            if (rolesBeforeChange.contains(UserRoleEnum.ROLE_ADMIN) && !roles.contains(UserRoleEnum.ROLE_ADMIN)) {
                adminEvent = NoticeTypeEnum.ADMIN_REMOVED;
            } else if (!rolesBeforeChange.contains(UserRoleEnum.ROLE_ADMIN) && roles.contains(UserRoleEnum.ROLE_ADMIN)) {
                adminEvent = NoticeTypeEnum.ADMIN_ADDED;
            }

            if (adminEvent != null) {
                NoticeTypeEnum finalAdminEvent = adminEvent;
                userService.getUser(userId).ifPresent(user -> {
                    String eventText = finalAdminEvent.equals(NoticeTypeEnum.ADMIN_ADDED)
                            ? NoticeService.TEXT_ADMIN_ADDED : NoticeService.TEXT_ADMIN_REMOVED;

                    NoticeData noticeData = new NoticeData(new NoticeUser(user.getId(), user.getFirstName(), user.getLastName()));

                    List<User> admins = userService.getUsersByRole(UserRoleEnum.ROLE_ADMIN);

                    final Notice notice = new Notice();
                    notice.setType(finalAdminEvent);
                    notice.setText(eventText);
                    notice.setData(noticeData);
                    notice.setCreatedBy(currentUserId);

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
        int currentUserId = CommonUtils.getCurrentUserId();

        Map<Integer, List<TrackUnitProjectTask>> idUsersToNoticeData = trackUnitService.getTrackUnitsInfo(trackUnitIds)
                .stream()
                .collect(
                        Collectors.groupingBy(
                                TrackUnitProjectTask::getUserId
                        )
                );

        if (idUsersToNoticeData.isEmpty()) {
            return;
        }

        Map<Integer, NoticeTrackUnitReject> userIdsToNoticeData = new HashMap<>();

        for (var userId : idUsersToNoticeData.keySet()) {
            var curTrackUnits = idUsersToNoticeData.get(userId);

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

            userIdsToNoticeData.put(userId, new NoticeTrackUnitReject(minDate, maxDate));
        }

        List<User> users = userService.getUsers(userIdsToNoticeData.keySet());

        for (var user : users) {
            Notice notice = new Notice();
            notice.setType(NoticeTypeEnum.HOURS_REJECTED);
            notice.setText(NoticeService.TEXT_HOURS_REJECTED);
            notice.setData(userIdsToNoticeData.get(user.getId()));
            notice.setCreatedBy(currentUserId);

            notifierObservable.notifyAllChannels(List.of(user), notice);
        }
    }

    @Pointcut(value = "execution(* ru.smartup.timetracker.service.TrackUnitService.freezeAllByDate(..)) && args(date, ..)",
            argNames = "date")
    public void callCompleteFreezeTrackUnits(LocalDate date) {
    }

    @AfterReturning(value = "callCompleteFreezeTrackUnits(date)", argNames = "date")
    public void sendNoticeFreezeTracksSuccess(LocalDate date) {
        final int currentUserId = CommonUtils.getCurrentUserId();

        List<User> users = userService.getUsersByRoles(List.of(UserRoleEnum.ROLE_ADMIN, UserRoleEnum.ROLE_REPORT_RECEIVER));

        Notice notice = new Notice();
        notice.setType(NoticeTypeEnum.FREEZE_SUCCESS);
        notice.setText(NoticeService.TEXT_FREEZE_SUCCESS);
        notice.setData(new NoticeData(date));
        notice.setCreatedBy(currentUserId);

        notifierObservable.notifyAllChannels(users, notice);
    }

    @AfterThrowing(value = "callCompleteFreezeTrackUnits(date)", argNames = "date, throwable", throwing = "throwable")
    public void sendNoticeFreezeTracksError(LocalDate date, Throwable throwable) {
        final int currentUserId = CommonUtils.getCurrentUserId();

        List<User> users = userService.getUsersByRole(UserRoleEnum.ROLE_ADMIN);

        Notice notice = new Notice();
        notice.setType(NoticeTypeEnum.FREEZE_ERROR);
        notice.setText(NoticeService.TEXT_FREEZE_ERROR);
        notice.setData(new NoticeData(date, throwable.getMessage()));
        notice.setCreatedBy(currentUserId);

        notifierObservable.notifyAllChannels(users, notice);
    }

    @Pointcut(value = "execution(* ru.smartup.timetracker.service.freeze.FreezeScheduler.scheduleFreeze(..)) && args(freezeRecord, ..)",
    argNames = "freezeRecord")
    public void callScheduleFreeze(FreezeRecord freezeRecord) {
    }

    @After(value = "callScheduleFreeze(freezeRecord)", argNames = "freezeRecord")
    public void sendNoticeFutureFreeze(FreezeRecord freezeRecord) {
        noticeScheduleService.scheduleFreezeNotice(freezeRecord);
    }

    @Pointcut(value = "execution(* ru.smartup.timetracker.service.freeze.FreezeScheduler.cancelFreezeTasks())")
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
        var users = userService.getNotArchivedUsers();
        List<User> admins = userService.getUsersByRole(UserRoleEnum.ROLE_ADMIN);

        String time = DateUtils.formatZoneDate(freezeDateUtils.getZoneUnfreezingTimestamp());

        Notice notice = new Notice();
        notice.setData(
                new NoticeUnfreeze(unfreezeRecord.getFreezeDate(), time)
        );
        notice.setText(NoticeService.TEXT_UN_FREEZE);
        notice.setType(NoticeTypeEnum.UN_FREEZE);
        notice.setCreatedBy(admins.get(0).getId());

        notifierObservable.notifyAllChannels(users, notice);
    }
}
