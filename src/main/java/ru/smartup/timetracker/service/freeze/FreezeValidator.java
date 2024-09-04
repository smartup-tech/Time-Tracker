package ru.smartup.timetracker.service.freeze;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.smartup.timetracker.core.freeze.ScheduleFreezeProperties;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.field.enumerated.FreezeRecordStatusEnum;
import ru.smartup.timetracker.repository.FreezeRecordRepository;

import java.time.LocalDate;

@Component
@AllArgsConstructor
public class FreezeValidator {
    private final ScheduleFreezeProperties scheduleProps;
    private final FreezeRecordRepository freezeRecordRepository;

    public boolean canUnfreeze(final FreezeRecord unfreezeRecord, final LocalDate nextFreezeDate) {
        if (nextFreezeDate == null) {
            return true;
        }

        final LocalDate freezeDateAfterUnfreeze = unfreezeRecord.getFreezeDate().plusDays(scheduleProps.getDayToUnfreeze());

        return freezeDateAfterUnfreeze.isBefore(nextFreezeDate);
    }

    public boolean hasNoUnfreezeRecord() {
        return freezeRecordRepository.existsByStatus(FreezeRecordStatusEnum.UN_FREEZE);
    }
}
