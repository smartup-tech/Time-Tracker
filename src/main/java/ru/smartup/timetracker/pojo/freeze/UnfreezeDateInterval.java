package ru.smartup.timetracker.pojo.freeze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnfreezeDateInterval {
    private LocalDate startDate;
    private LocalDate endDate;
}
