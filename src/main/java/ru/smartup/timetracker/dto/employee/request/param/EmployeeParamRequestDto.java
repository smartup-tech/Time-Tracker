package ru.smartup.timetracker.dto.employee.request.param;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class EmployeeParamRequestDto {
    private String query = "";
    private boolean archive = false;
}
