package ru.smartup.timetracker.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.smartup.timetracker.core.freeze.ScheduleFreezeProperties;
import ru.smartup.timetracker.entity.FreezeRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class FreezeDateUtils {

    private final ScheduleFreezeProperties freezeProperties;

    public ZonedDateTime getZoneTimestampNow() {
        return ZonedDateTime.now(freezeProperties.getTimeZone());
    }

    public ZonedDateTime getZoneFreezingTimestamp(final FreezeRecord freezeRecord) {
        return ZonedDateTime.of(
                LocalDateTime.of(freezeRecord.getFreezeDate(), freezeProperties.getFixedTimeFreeze()),
                freezeProperties.getTimeZone());
    }

    public ZonedDateTime getZoneUnfreezingTimestamp() {
        final LocalDate freezeDate = getZoneTimestampNow().plusDays(freezeProperties.getDayToUnfreeze()).toLocalDate();
        return ZonedDateTime.of(
                freezeDate, freezeProperties.getFixedTimeFreeze(),
                freezeProperties.getTimeZone());
    }

    public ZonedDateTime getMinZoneTimestamp() {
        return ZonedDateTime.of(LocalDateTime.MIN, freezeProperties.getTimeZone());
    }

    public ZonedDateTime getMaxZoneTimestamp() {
        return ZonedDateTime.of(LocalDateTime.MAX, freezeProperties.getTimeZone());
    }

    public Optional<FreezeRecord> getFreezeRecordWithMinDate (final List<FreezeRecord> freezeRecords) {
        return freezeRecords
                .stream()
                .min(Comparator.comparing(FreezeRecord::getFreezeDate));
    }

    public ZoneId getZoneId() {
        return freezeProperties.getTimeZone();
    }

    public boolean isEqualLocalDate(final LocalDate l1, final LocalDate l2) {
        return l1.isEqual(l2);
    }


}
