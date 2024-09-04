package ru.smartup.timetracker.dto.approval.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubmittedWorkDaysTableUnitDto {
    private int userId;
    private String firstName;
    private String lastName;
    private List<SubmittedSummaryWorkDay> summaryTrackUnits;
    private List<SubmittedWorkDaysTableProjectUnitDto> projectTrackUnits;

    public SubmittedWorkDaysTableUnitDto(final int userId) {
        this.userId = userId;
    }
}
