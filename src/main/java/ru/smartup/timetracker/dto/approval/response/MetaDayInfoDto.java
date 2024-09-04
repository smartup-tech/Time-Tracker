package ru.smartup.timetracker.dto.approval.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.ProductionCalendarDayEnum;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MetaDayInfoDto {
    private LocalDate date;

    private ProductionCalendarDayEnum status;

    private float standardHours;

    public MetaDayInfoDto(final LocalDate date) {
        this.date = date;
    }
}
