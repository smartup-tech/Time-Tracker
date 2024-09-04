package ru.smartup.timetracker.dto.production.calendar.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.ProductionCalendarDayEnum;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductionCalendarAddDayDto {
    @NotNull
    private Date day;
    @NotNull
    private ProductionCalendarDayEnum status;
    @DecimalMin("0.0")
    @DecimalMax("8.0")
    private float hours;
}
