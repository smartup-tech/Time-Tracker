package ru.smartup.timetracker.repository;

import ru.smartup.timetracker.entity.Notice;

import java.util.List;

public interface NoticeBatchRepository {
    void createNotices(List<Notice> notices);
}