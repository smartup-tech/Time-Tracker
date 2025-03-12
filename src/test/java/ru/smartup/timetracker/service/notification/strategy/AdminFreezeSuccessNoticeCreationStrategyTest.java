package ru.smartup.timetracker.service.notification.strategy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.smartup.timetracker.dto.notice.NoticeCreationDto;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;
import ru.smartup.timetracker.service.notification.NoticeService;

class AdminFreezeSuccessNoticeCreationStrategyTest {

    @Test
    public void shouldCreateFreezeSuccessNoticeForAdmin() {
        NoticeCreationDto noticeCreationDto = NoticeCreationDto.builder()
                .createdBy(1)
                .build();

        Notice expected = new Notice();
        expected.setType(NoticeTypeEnum.FREEZE_SUCCESS);
        expected.setText(NoticeService.TEXT_FREEZE_SUCCESS);
        expected.setCreatedBy(noticeCreationDto.getCreatedBy());

        AdminFreezeSuccessNoticeCreationStrategy strategy = new AdminFreezeSuccessNoticeCreationStrategy();
        Notice actual = strategy.createNotice(noticeCreationDto);

        Assertions.assertThat(actual)
                .extracting(Notice::getType, Notice::getText, Notice::getCreatedBy)
                .containsExactly(expected.getType(), expected.getText(), expected.getCreatedBy());
    }

    @Test
    public void shouldReturnRoleAdminEnum() {
        AdminFreezeSuccessNoticeCreationStrategy strategy = new AdminFreezeSuccessNoticeCreationStrategy();
        EmployeeRoleEnum expected = EmployeeRoleEnum.ROLE_ADMIN;
        EmployeeRoleEnum actual = strategy.getRole();

        Assertions.assertThat(actual).isEqualTo(expected);
    }
}
