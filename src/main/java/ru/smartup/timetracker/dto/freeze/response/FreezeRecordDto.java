package ru.smartup.timetracker.dto.freeze.response;

import lombok.Data;
import ru.smartup.timetracker.dto.employee.response.EmployeeShortDto;
import ru.smartup.timetracker.entity.field.enumerated.FreezeRecordStatusEnum;

import java.time.LocalDate;

@Data
public class FreezeRecordDto {
    private LocalDate freezeDate;

    private FreezeRecordStatusEnum status;

    private EmployeeShortDto employees;
}
