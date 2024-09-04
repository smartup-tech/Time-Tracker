package ru.smartup.timetracker.dto.position.request;

import lombok.Data;
import ru.smartup.timetracker.validation.RequiredField;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;

@Data
public class PositionCreateDto {
    @RequiredField(maxSize = 255)
    private String name;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    private float externalRate;
}
