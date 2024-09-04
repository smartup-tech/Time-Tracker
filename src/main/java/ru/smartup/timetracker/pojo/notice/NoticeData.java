package ru.smartup.timetracker.pojo.notice;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class NoticeData {
    private NoticeProject project;

    private NoticeUser user;

    private NoticeTask task;

    private NoticeTrackUnitReject unit;

    private NoticeNumberUsersHours usersHours;

    private Map<String, NoticeChanges> changes;

    private LocalDate date;

    private String error;

    public NoticeData(NoticeProject project) {
        this.project = project;
    }

    public NoticeData(NoticeUser user) {
        this.user = user;
    }

    public NoticeData(NoticeProject project, NoticeUser user) {
        this.project = project;
        this.user = user;
    }

    public NoticeData(NoticeNumberUsersHours usersHours) {
        this.usersHours = usersHours;
    }

    public NoticeData(NoticeProject project, NoticeTask task, NoticeTrackUnitReject unit) {
        this.project = project;
        this.task = task;
        this.unit = unit;
    }

    public NoticeData(LocalDate date) {
        this.date = date;
    }

    public NoticeData(LocalDate date, String error) {
        this.date = date;
        this.error = error;
    }

    public void addChange(String fieldName, NoticeChanges changes) {
        if (this.changes == null) {
            this.changes = new HashMap<>();
        }
        this.changes.put(fieldName, changes);
    }
}
