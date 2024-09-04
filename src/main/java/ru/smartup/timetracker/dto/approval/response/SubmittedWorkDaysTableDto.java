package ru.smartup.timetracker.dto.approval.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubmittedWorkDaysTableDto {
    private List<MetaDayInfoDto> days;
    private List<SubmittedWorkDaysTableUnitDto> data;
}
