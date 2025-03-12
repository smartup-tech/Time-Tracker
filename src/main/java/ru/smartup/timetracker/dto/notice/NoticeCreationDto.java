package ru.smartup.timetracker.dto.notice;

import lombok.Builder;
import lombok.Data;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;

@Data
@Builder
public class NoticeCreationDto {
    private NoticeTypeEnum type;
    private String text;
    private Object data;
    private int createdBy;
}