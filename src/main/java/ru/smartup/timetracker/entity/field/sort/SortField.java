package ru.smartup.timetracker.entity.field.sort;

public interface SortField {
    SortField idField = () -> new String[]{"id"};
    String[] getValues();
}
