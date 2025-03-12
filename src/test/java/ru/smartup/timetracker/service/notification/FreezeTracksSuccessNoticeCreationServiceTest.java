package ru.smartup.timetracker.service.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
import ru.smartup.timetracker.service.notification.strategy.AdminFreezeSuccessNoticeCreationStrategy;
import ru.smartup.timetracker.service.notification.strategy.NoticeCreationStrategy;
import ru.smartup.timetracker.service.notification.strategy.ReportReceiverFreezeSuccessNoticeCreationStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


class FreezeTracksSuccessNoticeCreationServiceTest {
    private static FreezeTracksSuccessNoticeCreationService freezeTracksSuccessNoticeCreationService;

    @BeforeAll
    static void setUpBeforeClass() {
        List<NoticeCreationStrategy> noticeCreationStrategies = List.of(new AdminFreezeSuccessNoticeCreationStrategy(), new ReportReceiverFreezeSuccessNoticeCreationStrategy());

        freezeTracksSuccessNoticeCreationService = new FreezeTracksSuccessNoticeCreationService(noticeCreationStrategies);
    }

    @Test
    public void shouldReturnAdminFreezeSuccessNoticeCreationStrategy() {
        Optional<NoticeCreationStrategy> actual = freezeTracksSuccessNoticeCreationService.getStrategy(EmployeeRoleEnum.ROLE_ADMIN);

        Assertions.assertThat(actual.isPresent()).isTrue();
        Assertions.assertThat(actual.get()).isInstanceOf(AdminFreezeSuccessNoticeCreationStrategy.class);
    }

    @Test
    public void shouldReturnReportFreezeSuccessNoticeCreationStrategy() {
        Optional<NoticeCreationStrategy> actual = freezeTracksSuccessNoticeCreationService.getStrategy(EmployeeRoleEnum.ROLE_REPORT_RECEIVER);

        Assertions.assertThat(actual.isPresent()).isTrue();
        Assertions.assertThat(actual.get()).isInstanceOf(ReportReceiverFreezeSuccessNoticeCreationStrategy.class);
    }
}
