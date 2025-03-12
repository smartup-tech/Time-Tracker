package ru.smartup.timetracker.service.notification.strategy;

import org.springframework.stereotype.Component;
import ru.smartup.timetracker.dto.notice.NoticeCreationDto;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;
import ru.smartup.timetracker.service.notification.NoticeService;

@Component
public class ReportReceiverFreezeSuccessNoticeCreationStrategy implements NoticeCreationStrategy {
    @Override
    public Notice createNotice(NoticeCreationDto noticeCreationDto) {
        Notice notice = new Notice();
        notice.setType(NoticeTypeEnum.FREEZE_SUCCESS);
        notice.setText(NoticeService.TEXT_FREEZE_SUCCESS);
        notice.setData(noticeCreationDto.getData());
        notice.setCreatedBy(noticeCreationDto.getCreatedBy());

        return notice;
    }

    @Override
    public EmployeeRoleEnum getRole() {
        return EmployeeRoleEnum.ROLE_REPORT_RECEIVER;
    }
}