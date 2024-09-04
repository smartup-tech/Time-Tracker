package ru.smartup.timetracker.pojo.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NoticePersonalToken {
    private final String username;
    private final String token;
    private final long ttlInHours;
}
