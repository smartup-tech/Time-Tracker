package ru.smartup.timetracker.pojo.notice;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;



@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class NoticeFreeze {
    private String date;

    public NoticeFreeze(String date) {
        this.date = date;
    }
}
