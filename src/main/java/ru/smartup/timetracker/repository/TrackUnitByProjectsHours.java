package ru.smartup.timetracker.repository;

public interface TrackUnitByProjectsHours {
    int getProjectId();

    String getProjectName();

    float getSubmittedHours();

    float getTotalHours();
}
