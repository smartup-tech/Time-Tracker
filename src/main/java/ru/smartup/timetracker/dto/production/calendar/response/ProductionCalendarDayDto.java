package ru.smartup.timetracker.dto.production.calendar.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.ProductionCalendarDayEnum;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductionCalendarDayDto {
    private long id;
    private Date day;
    private ProductionCalendarDayEnum status;
    private float hours;
}
