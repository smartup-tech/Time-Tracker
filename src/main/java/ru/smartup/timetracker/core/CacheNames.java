package ru.smartup.timetracker.core;

public class CacheNames {
    public static class FreezeRecord {
        public static final String GET_RECORDS = "freeze_record_get_records";
        public static final String GET_WITH_MAX_FREEZE_DATE = "freeze_record_get_with_max_freeze_date";
    }

    public static class Position {
        public static final String GET_ACTIVE_POSITIONS = "position_get_active_positions";
        public static final String GET_POSITIONS_SEARCH_PAGEABLE = "position_get_positions_search_pageable";
        public static final String GET_POSITION_BY_ID = "position_get_by_id";
        public static final String GET_NOT_ARCHIVED_POSITION_BY_ID = "position_get_not_archived_by_id";
    }

    public static class Project {
        public static final String GET_ACTIVE_PROJECTS = "project_get_active_projects";
        public static final String GET_ALL_PROJECTS = "project_get_all_projects";
        public static final String GET_PROJECTS_SEARCH_PAGEABLE = "project_get_projects_search_pageable";
        public static final String GET_PROJECT_BY_ID = "project_get_by_id";
        public static final String GET_NOT_ARCHIVED_PROJECT_BY_ID = "project_get_not_archived_by_id";
    }
}
