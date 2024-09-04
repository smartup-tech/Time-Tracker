package ru.smartup.timetracker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@NoArgsConstructor
@Setter
@Getter
public class PageableRequestParamDto<T> {
    private int page = 0;
    private int size = 10;
    private T sortBy;
    private Sort.Direction sortDirection = Sort.Direction.DESC;
}
