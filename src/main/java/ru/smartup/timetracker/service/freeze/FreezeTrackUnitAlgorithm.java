package ru.smartup.timetracker.service.freeze;

import ru.smartup.timetracker.entity.FreezeRecord;

public interface FreezeTrackUnitAlgorithm {
    void freeze(final FreezeRecord freezeRecord);
}
