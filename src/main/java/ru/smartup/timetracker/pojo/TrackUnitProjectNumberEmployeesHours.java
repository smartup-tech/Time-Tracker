package ru.smartup.timetracker.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrackUnitProjectNumberEmployeesHours {
    private int projectId;

    private long numberEmployees;

    private double sumHours;
}
