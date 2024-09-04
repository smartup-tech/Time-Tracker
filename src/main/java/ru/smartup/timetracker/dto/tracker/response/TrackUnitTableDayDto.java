package ru.smartup.timetracker.dto.tracker.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.dto.approval.response.MetaDayInfoDto;
import ru.smartup.timetracker.entity.field.enumerated.ProductionCalendarDayEnum;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackUnitTableDayDto extends MetaDayInfoDto  {
    private boolean blocked;

    public TrackUnitTableDayDto(final LocalDate date, final ProductionCalendarDayEnum status, final float standardHours, final boolean blocked) {
        super(date, status, standardHours);
        this.blocked = blocked;
    }
}
