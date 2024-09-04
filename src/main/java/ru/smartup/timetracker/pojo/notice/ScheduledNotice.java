package ru.smartup.timetracker.pojo.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.smartup.timetracker.entity.Notice;

import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;

@AllArgsConstructor
@Getter
public class ScheduledNotice {

    private ZonedDateTime time;
    private ScheduledFuture<?> scheduledFuture;

}
