package ru.smartup.timetracker.dto.notice.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;

import java.sql.Timestamp;

@Data
public class NoticeDto {
    private long id;

    private NoticeTypeEnum type;

    private String text;

    private Object data;

    private boolean read;

    private int createdBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createdDate;
}
