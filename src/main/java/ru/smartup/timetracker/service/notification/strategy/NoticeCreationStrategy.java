package ru.smartup.timetracker.service.notification.strategy;

import ru.smartup.timetracker.dto.notice.NoticeCreationDto;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;

public interface NoticeCreationStrategy {
    Notice createNotice(NoticeCreationDto noticeCreationDto);

    EmployeeRoleEnum getRole();
}
