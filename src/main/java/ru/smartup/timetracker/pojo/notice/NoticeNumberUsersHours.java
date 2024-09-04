package ru.smartup.timetracker.pojo.notice;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class NoticeNumberUsersHours {
    private int projectId;

    private long numberUsers;

    private double sumHours;
}
