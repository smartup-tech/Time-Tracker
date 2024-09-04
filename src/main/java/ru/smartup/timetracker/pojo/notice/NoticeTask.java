package ru.smartup.timetracker.pojo.notice;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Data
public class NoticeTask {
    private Long id;

    private String name;
}
