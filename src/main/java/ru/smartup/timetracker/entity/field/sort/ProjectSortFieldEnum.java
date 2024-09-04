package ru.smartup.timetracker.entity.field.sort;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectSortFieldEnum implements SortField {
    @JsonProperty("id")
    ID(new String[]{"id"}),
    @JsonProperty("name")
    NAME(new String[]{"name"});

    private final String[] values;
}
