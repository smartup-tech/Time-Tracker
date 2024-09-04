package ru.smartup.timetracker.service.freeze;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.pojo.ScheduledFreeze;
import ru.smartup.timetracker.utils.FreezeDateUtils;

import java.time.*;

@Slf4j
@Service
/**
 * Не потокобезопасный!
 * Возможно состояние гонки за scheduledFreeze.
 * */
public class FreezeScheduler {

    private final TaskScheduler taskScheduler;
    private final FreezeDateUtils freezeDateUtils;

    private ScheduledFreeze scheduledFreeze = null;
    private ScheduledFreeze scheduledUnfreeze = null;

    public FreezeScheduler(final TaskScheduler taskScheduler,
                           final FreezeDateUtils freezeDateUtils) {
        this.taskScheduler = taskScheduler;
        this.freezeDateUtils = freezeDateUtils;
    }

    public void scheduleFreeze(final FreezeRecord freezeRecord, final FreezeTrackUnitAlgorithm freeze) {
        final ZonedDateTime freezeTime = freezeDateUtils.getZoneFreezingTimestamp(freezeRecord);
        scheduleFreeze(freezeRecord, freezeTime, freeze);
    }

    public void scheduleFreeze(final FreezeRecord freezeRecord, final ZonedDateTime freezeTime, final FreezeTrackUnitAlgorithm freeze) {
        log.info("FREEZING TIME {}", freezeTime.toInstant());

        var scheduledTask = taskScheduler.schedule(
                () -> freeze.freeze(freezeRecord), freezeTime.toInstant()
        );

        scheduledFreeze = new ScheduledFreeze(scheduledTask, freezeRecord.getFreezeDate());
    }

    public void cancelFreezeTask() {
        if (scheduledFreeze == null) {
            return;
        }
        scheduledFreeze.getScheduledFuture().cancel(true);
        scheduledFreeze = null;
    }

    public void unfreeze(final FreezeRecord unfreezeRecord, final FreezeTrackUnitAlgorithm freeze) {
        final ZonedDateTime freezeTimeUnfreezingRecord = freezeDateUtils.getZoneUnfreezingTimestamp();

        log.info("UNFREEZING TIME {}", freezeTimeUnfreezingRecord.toInstant());

        var scheduledTask = taskScheduler.schedule(
                () -> freeze.freeze(unfreezeRecord), freezeTimeUnfreezingRecord.toInstant()
        );

        scheduledUnfreeze = new ScheduledFreeze(scheduledTask, freezeTimeUnfreezingRecord.toLocalDate());
    }

    public boolean isScheduled() {
        return scheduledFreeze != null;
    }

    public boolean scheduledDateEarlierThan(final LocalDate localDate) {
        return scheduledFreeze.getFreezeDate().isBefore(localDate);
    }

}
