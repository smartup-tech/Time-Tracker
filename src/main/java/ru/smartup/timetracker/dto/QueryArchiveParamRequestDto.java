package ru.smartup.timetracker.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class QueryArchiveParamRequestDto {
    private String query = "";
    private boolean archive = false;
}
