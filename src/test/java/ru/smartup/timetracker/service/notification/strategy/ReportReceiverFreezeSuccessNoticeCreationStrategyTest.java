package ru.smartup.timetracker.service.notification.strategy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.smartup.timetracker.dto.notice.NoticeCreationDto;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;
import ru.smartup.timetracker.pojo.notice.NoticeData;
import ru.smartup.timetracker.service.notification.NoticeService;

import java.time.LocalDate;

class ReportReceiverFreezeSuccessNoticeCreationStrategyTest {

    @Test
    public void shouldCreateFreezeSuccessNoticeForReportReceiver() {
        NoticeCreationDto noticeCreationDto = NoticeCreationDto.builder()
                .createdBy(1)
                .data(new NoticeData(LocalDate.of(2024, 9, 25)))
                .build();

        Notice expected = new Notice();
        expected.setType(NoticeTypeEnum.FREEZE_SUCCESS);
        expected.setText(NoticeService.TEXT_FREEZE_SUCCESS);
        expected.setData(new NoticeData(LocalDate.of(2024, 9, 25)));
        expected.setCreatedBy(noticeCreationDto.getCreatedBy());

        ReportReceiverFreezeSuccessNoticeCreationStrategy strategy = new ReportReceiverFreezeSuccessNoticeCreationStrategy();
        Notice actual = strategy.createNotice(noticeCreationDto);

        Assertions.assertThat(actual)
                .extracting(Notice::getType, Notice::getText, Notice::getData, Notice::getCreatedBy)
                .containsExactly(expected.getType(), expected.getText(), expected.getData(), expected.getCreatedBy());
    }

    @Test
    public void shouldReturnRoleReportReceiverEnum() {
        ReportReceiverFreezeSuccessNoticeCreationStrategy strategy = new ReportReceiverFreezeSuccessNoticeCreationStrategy();
        EmployeeRoleEnum expected = EmployeeRoleEnum.ROLE_REPORT_RECEIVER;
        EmployeeRoleEnum actual = strategy.getRole();

        Assertions.assertThat(actual).isEqualTo(expected);
    }
}
