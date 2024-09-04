package ru.smartup.timetracker.dto.notice.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class NoticeScheduleUpdateDto {
    @NotNull
    Set<@Min(1) @Max(7) Integer> days;
}
