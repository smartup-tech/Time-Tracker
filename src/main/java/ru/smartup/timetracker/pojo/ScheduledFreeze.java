package ru.smartup.timetracker.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.concurrent.ScheduledFuture;

@AllArgsConstructor
@Getter
public class ScheduledFreeze {
    private final ScheduledFuture<?> scheduledFuture;
    private final LocalDate freezeDate;
}
