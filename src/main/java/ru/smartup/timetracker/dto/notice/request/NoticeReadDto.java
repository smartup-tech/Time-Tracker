package ru.smartup.timetracker.dto.notice.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
public class NoticeReadDto {
    @NotEmpty
    Set<Long> noticeIds;
}
