package ru.smartup.timetracker.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.pojo.notice.NoticeData;
import ru.smartup.timetracker.pojo.notice.NoticeFreeze;
import ru.smartup.timetracker.pojo.notice.ScheduledNotice;
import ru.smartup.timetracker.repository.UserRepository;
import ru.smartup.timetracker.service.UserService;
import ru.smartup.timetracker.service.notification.notifier.NotifierObservable;
import ru.smartup.timetracker.utils.DateUtils;
import ru.smartup.timetracker.utils.FreezeDateUtils;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class NoticeScheduleService {

    private final UserService userService;
    private final FreezeDateUtils freezeDateUtils;

    private final NotifierObservable notifierObservable;
    private final UserRepository userRepository;

    private final TaskScheduler taskScheduler;

    private ScheduledNotice scheduledNotice;

    public void scheduleFreezeNotice(final FreezeRecord freezeRecord) {
        int dayBeforeFreeze = 1;

        ZonedDateTime notificationTime = ZonedDateTime.of(freezeRecord.getFreezeDate().minusDays(dayBeforeFreeze), LocalTime.NOON, freezeDateUtils.getZoneId());
        ZonedDateTime now = freezeDateUtils.getZoneTimestampNow();

        List<User> admins = userRepository.findAllByUserRole(UserRoleEnum.ROLE_ADMIN);
        if (admins.isEmpty()) {
            return;
        }

        Notice notice = new Notice();
        notice.setType(NoticeTypeEnum.FREEZE_PREPARE);
        notice.setText(NoticeService.TEXT_FREEZE_PREPARE);
        notice.setData(new NoticeFreeze(DateUtils.formatZoneDate(notificationTime.plusDays(1))));
        notice.setCreatedBy(admins.get(0).getId());

        if (now.isAfter(notificationTime)) {
            var users = userService.getNotArchivedUsers();
            notifierObservable.notifyAllChannels(users, notice);
        } else {
            scheduleNotice(notice, notificationTime);
        }

    }

    public void scheduleNotice(Notice notice, ZonedDateTime sendingTime) {
        var users = userService.getNotArchivedUsers();

        var scheduledFuture = taskScheduler.schedule(
                () -> notifierObservable.notifyAllChannels(users, notice),
                sendingTime.toInstant()
        );

        log.info("Schedule Notice: {}.", notice);
        log.info("Sending time: {}.", sendingTime.toInstant());
        scheduledNotice = new ScheduledNotice(sendingTime, scheduledFuture);
    }

    public void cancelNotice() {
        if (scheduledNotice == null) {
            return;
        }

        if (!cancelFreezeNotificationSent()) {
            scheduledNotice.getScheduledFuture().cancel(true);
            scheduledNotice = null;
        }
    }

    private boolean cancelFreezeNotificationSent() {
        if (scheduledNotice.getTime().isBefore(freezeDateUtils.getZoneTimestampNow())) {
            List<User> admins = userRepository.findAllByUserRole(UserRoleEnum.ROLE_ADMIN);
            if (admins.isEmpty()) {
                return false;
            }

            Notice notice = new Notice();
            notice.setType(NoticeTypeEnum.FREEZE_CANCEL);
            notice.setText(NoticeService.TEXT_FREEZE_CANCEL);
            notice.setData(new NoticeData(scheduledNotice.getTime().toLocalDate()));
            notice.setCreatedBy(admins.get(0).getId());

            var users = userService.getNotArchivedUsers();

            notifierObservable.notifyAllChannels(users, notice);
            return true;
        }
        return false;
    }
}
