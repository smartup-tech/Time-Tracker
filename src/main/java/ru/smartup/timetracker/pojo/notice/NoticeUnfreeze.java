package ru.smartup.timetracker.pojo.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
public class NoticeUnfreeze {
    private LocalDate unfreezeRecordDate;
    private String freezeDate;
}
