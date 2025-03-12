package ru.smartup.timetracker.pojo.notice;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class NoticeNumberEmployeesHours {
    private int projectId;

    private long numberEmployees;

    private double sumHours;
}
