package ru.smartup.timetracker.service.freeze;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.smartup.timetracker.core.lock.LockManager;
import ru.smartup.timetracker.core.lock.LockNames;
import ru.smartup.timetracker.dto.freeze.request.FreezeDateDtoRequest;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.field.enumerated.FreezeRecordStatusEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.utils.FreezeDateUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FreezeService {
    private final FreezeSchedulePlanner freezeSchedulePlanner;
    private final CRUDFreezeService crudFreezeService;
    private final FreezeValidator freezeValidator;
    private final FreezeDateUtils freezeDateUtils;

    private final LockManager lockManager;

    public boolean createOrUpdateTask(final FreezeDateDtoRequest actualFreezeDate, final int userId) {
        List<FreezeRecord> allRecordsAfterLastFreeze = crudFreezeService.getFreezeRecords();
        Map<FreezeRecordStatusEnum, List<FreezeRecord>> waitingAndDeletingRecords = compareFreezeDatesWithActualAndMapByWaitingAndDeleting(allRecordsAfterLastFreeze, actualFreezeDate.getDates(), userId);
        return tryLockAndUpdateScheduleFreeze(waitingAndDeletingRecords);
    }

    private Map<FreezeRecordStatusEnum, List<FreezeRecord>> compareFreezeDatesWithActualAndMapByWaitingAndDeleting(final List<FreezeRecord> freezeRecords,
                                                                                                                   final List<LocalDate> actualDates,
                                                                                                                   final int userId) {
        final Map<LocalDate, FreezeRecord> dateToFreeze = freezeRecords
                .stream()
                .collect(Collectors.toMap(
                        FreezeRecord::getFreezeDate,
                        Function.identity()));

        final List<FreezeRecord> waitingRecords = new ArrayList<>();
        final List<FreezeRecord> deletingRecords = new ArrayList<>();

        for (final LocalDate date : actualDates) {
            FreezeRecord freezeRecord = dateToFreeze.get(date);
            if (freezeRecord == null) {
                final User user = new User();
                user.setId(userId);

                waitingRecords.add(new FreezeRecord(date, FreezeRecordStatusEnum.WAITING, user));
            } else {
                dateToFreeze.remove(date);
                waitingRecords.add(freezeRecord);
            }
        }
        dateToFreeze.forEach((date, record) -> deletingRecords.add(record));

        final Map<FreezeRecordStatusEnum, List<FreezeRecord>> statusToFreezeRecord = new HashMap<>();

        statusToFreezeRecord.put(FreezeRecordStatusEnum.WAITING, waitingRecords);
        statusToFreezeRecord.put(FreezeRecordStatusEnum.DELETING, deletingRecords);

        return statusToFreezeRecord;
    }

    private boolean tryLockAndUpdateScheduleFreeze(final Map<FreezeRecordStatusEnum, List<FreezeRecord>> waitingAndDeletingRecords) {
        final Lock lock = lockManager.getLock(LockNames.Freeze.TASK);
        if (lock.tryLock()) {
            try {
                updateScheduleFreeze(waitingAndDeletingRecords);
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    private void updateScheduleFreeze(final Map<FreezeRecordStatusEnum, List<FreezeRecord>> waitingAndDeletingRecords) {
        crudFreezeService.setNewFreezeSchedule(waitingAndDeletingRecords.get(FreezeRecordStatusEnum.WAITING), waitingAndDeletingRecords.get(FreezeRecordStatusEnum.DELETING));

        var nearestFreezeRecord = freezeDateUtils.getFreezeRecordWithMinDate(waitingAndDeletingRecords.get(FreezeRecordStatusEnum.WAITING));

        if (nearestFreezeRecord.isPresent()) {
            freezeSchedulePlanner.scheduleFreeze(nearestFreezeRecord.get());
        } else {
            freezeSchedulePlanner.cancel();
        }
    }

    public boolean unfreezeLastRecord() {
        final FreezeRecord unfreezeRecord = crudFreezeService.getCacheableLastFreeze();

        final List<LocalDate> boundaryUnfreezeRecord = crudFreezeService.getBoundaryFreezeRecord(unfreezeRecord);

        if (freezeValidator.hasNoUnfreezeRecord() && !freezeValidator.canUnfreeze(unfreezeRecord, boundaryUnfreezeRecord.get(1))) {
            return false;
        }

        freezeSchedulePlanner.unfreeze(unfreezeRecord, boundaryUnfreezeRecord.get(0));
        return true;
    }

}
