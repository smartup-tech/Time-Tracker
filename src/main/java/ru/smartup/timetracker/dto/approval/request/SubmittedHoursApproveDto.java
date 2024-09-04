package ru.smartup.timetracker.dto.approval.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class SubmittedHoursApproveDto {
    @NotEmpty
    private List<Long> trackUnitIds;
}
