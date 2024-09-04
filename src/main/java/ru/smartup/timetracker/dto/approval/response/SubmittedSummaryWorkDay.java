package ru.smartup.timetracker.dto.approval.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubmittedSummaryWorkDay {
    private Date date;
    private float hours;
}