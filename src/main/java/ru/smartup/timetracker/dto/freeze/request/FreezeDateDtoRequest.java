package ru.smartup.timetracker.dto.freeze.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreezeDateDtoRequest {
    private List<LocalDate> dates;
}
