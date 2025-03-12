package ru.smartup.timetracker.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;
import ru.smartup.timetracker.pojo.notice.NoticeData;
import ru.smartup.timetracker.pojo.notice.NoticeFreeze;
import ru.smartup.timetracker.pojo.notice.ScheduledNotice;
import ru.smartup.timetracker.repository.EmployeeRepository;
import ru.smartup.timetracker.service.EmployeeService;
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

    private final EmployeeService employeeService;
    private final FreezeDateUtils freezeDateUtils;

    private final NotifierObservable notifierObservable;
    private final EmployeeRepository employeeRepository;

    private final TaskScheduler taskScheduler;

    private ScheduledNotice scheduledNotice;

    public void scheduleFreezeNotice(final FreezeRecord freezeRecord) {
        int dayBeforeFreeze = 1;

        ZonedDateTime notificationTime = ZonedDateTime.of(freezeRecord.getFreezeDate().minusDays(dayBeforeFreeze), LocalTime.NOON, freezeDateUtils.getZoneId());
        ZonedDateTime now = freezeDateUtils.getZoneTimestampNow();

        List<Employee> admins = employeeRepository.findAllByEmployeeRole(EmployeeRoleEnum.ROLE_ADMIN);
        if (admins.isEmpty()) {
            return;
        }

        Notice notice = new Notice();
        notice.setType(NoticeTypeEnum.FREEZE_PREPARE);
        notice.setText(NoticeService.TEXT_FREEZE_PREPARE);
        notice.setData(new NoticeFreeze(DateUtils.formatZoneDate(notificationTime.plusDays(1))));
        notice.setCreatedBy(admins.get(0).getId());

        if (now.isAfter(notificationTime)) {
            var employees = employeeService.getNotArchivedEmployees();
            notifierObservable.notifyAllChannels(employees, notice);
        } else {
            scheduleNotice(notice, notificationTime);
        }

    }

    public void scheduleNotice(Notice notice, ZonedDateTime sendingTime) {
        var employees = employeeService.getNotArchivedEmployees();

        var scheduledFuture = taskScheduler.schedule(
                () -> notifierObservable.notifyAllChannels(employees, notice),
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
            List<Employee> admins = employeeRepository.findAllByEmployeeRole(EmployeeRoleEnum.ROLE_ADMIN);
            if (admins.isEmpty()) {
                return false;
            }

            Notice notice = new Notice();
            notice.setType(NoticeTypeEnum.FREEZE_CANCEL);
            notice.setText(NoticeService.TEXT_FREEZE_CANCEL);
            notice.setData(new NoticeData(scheduledNotice.getTime().toLocalDate()));
            notice.setCreatedBy(admins.get(0).getId());

            var employees = employeeService.getNotArchivedEmployees();

            notifierObservable.notifyAllChannels(employees, notice);
            return true;
        }
        return false;
    }
}
