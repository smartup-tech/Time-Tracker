package ru.smartup.timetracker.service.freeze;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.field.enumerated.FreezeRecordStatusEnum;
import ru.smartup.timetracker.service.TrackUnitService;
import ru.smartup.timetracker.utils.FreezeDateUtils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class FreezeSchedulePlanner implements FreezeTrackUnitAlgorithm {
    private final CRUDFreezeService crudFreezeService;
    private final FreezeDateUtils freezeDateUtils;
    private final FreezeScheduler freezeScheduler;
    private final TrackUnitService trackUnitService;

    @EventListener(ApplicationReadyEvent.class)
    protected void recoveryScheduled() {
        final List<FreezeRecord> allRecordsAfterLastFreeze = crudFreezeService.getFreezeRecords();

        final ZonedDateTime zoneNow = freezeDateUtils.getZoneTimestampNow();
        ZonedDateTime laterOutdatedFreezeDate = freezeDateUtils.getMinZoneTimestamp();
        FreezeRecord outdatedFreezeRecord = null;

        for (final FreezeRecord freezeRecord : allRecordsAfterLastFreeze) {
            final ZonedDateTime freezingTimestamp = freezeDateUtils.getZoneFreezingTimestamp(freezeRecord);

            final boolean isOutdated = freezingTimestamp.isBefore(zoneNow);
            final boolean isLaterOutdated = isOutdated && freezingTimestamp.isAfter(laterOutdatedFreezeDate);

            if (isLaterOutdated) {
                laterOutdatedFreezeDate = freezingTimestamp;
                outdatedFreezeRecord = freezeRecord;
            }
        }

        if (outdatedFreezeRecord != null) {
            freeze(outdatedFreezeRecord);
        } else {
            runNewFreeze();
        }

        FreezeRecord freezeRecord = crudFreezeService.getUnfreezeRecord();
        if (freezeRecord != null) {
            setFreezeFlagsInDb(freezeRecord);
        }
    }

    public void scheduleFreeze(final FreezeRecord freezeRecord) {
        final boolean scheduledFreezeEarlier = freezeScheduler.isScheduled() && freezeScheduler.scheduledDateEarlierThan(freezeRecord.getFreezeDate());
        if (scheduledFreezeEarlier) {
            return;
        }

        if (freezeScheduler.isScheduled()) {
            freezeScheduler.cancelFreezeTask();
        }
        freezeScheduler.scheduleFreeze(freezeRecord, this);
    }

    private void runNewFreeze() {
        FreezeRecord nextFreeze = crudFreezeService.getFreezeWithMinDateByStatus(FreezeRecordStatusEnum.WAITING);
        if (nextFreeze != null) {
            freezeScheduler.scheduleFreeze(nextFreeze, this);
        }
    }

    public void freeze(final FreezeRecord freezeRecord) {
        setFreezeFlagsInDb(freezeRecord);
        runNewFreeze();
    }

    private void setFreezeFlagsInDb(final FreezeRecord freezeRecord) {
        freezeRecord.setStatus(FreezeRecordStatusEnum.IN_PROGRESS);
        crudFreezeService.save(freezeRecord);

        freezeTrackUnits(freezeRecord);
    }

    private void freezeTrackUnits(final FreezeRecord freezeRecord) {
        try {
            int updatedRecords = trackUnitService.freezeAllByDate(freezeRecord.getFreezeDate());
            freezeRecord.successful(updatedRecords);
        } catch (Exception e) {
            freezeRecord.interrupted(e.getMessage());
        } finally {
            crudFreezeService.save(freezeRecord);
        }
    }

    public void cancel() {
        freezeScheduler.cancelFreezeTask();
    }


    public void unfreeze(final FreezeRecord unfreezeRecord, final LocalDate prevFreezeRecord) {
        if (prevFreezeRecord != null && unfreezeRecord.getFreezeDate().isBefore(prevFreezeRecord)) {
            return;
        }

        unfreezeRecord.setStatus(FreezeRecordStatusEnum.UN_FREEZE);
        crudFreezeService.save(unfreezeRecord);

        final LocalDate unfreezeDate = prevFreezeRecord == null ? unfreezeRecord.getFreezeDate() : prevFreezeRecord;

        tryUnfreeze(unfreezeRecord, unfreezeDate);
    }

    private void tryUnfreeze(final FreezeRecord unfreezeRecord, final LocalDate unfreezeDate) {
        try {
            final int updatedRecords = trackUnitService.unfreezeAllByDate(unfreezeDate);

            unfreezeRecord.unfreeze(updatedRecords);

            freezeScheduler.unfreeze(unfreezeRecord, this);

        } catch (Exception e) {
            unfreezeRecord.interrupted(e.getMessage());
        } finally {
            crudFreezeService.save(unfreezeRecord);
        }
    }
}
