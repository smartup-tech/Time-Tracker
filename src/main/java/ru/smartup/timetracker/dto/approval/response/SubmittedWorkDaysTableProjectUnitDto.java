package ru.smartup.timetracker.dto.approval.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmittedWorkDaysTableProjectUnitDto {
    private int projectId;

    private String projectName;

    private long taskId;

    private String taskName;

    private List<SubmittedWorkDayUnitDto> trackUnits;

    public SubmittedWorkDaysTableProjectUnitDto(final long taskId) {
        this.taskId = taskId;
    }
}
