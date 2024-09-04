package ru.smartup.timetracker.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrackUnitProjectNumberUsersHours {
    private int projectId;

    private long numberUsers;

    private double sumHours;
}
