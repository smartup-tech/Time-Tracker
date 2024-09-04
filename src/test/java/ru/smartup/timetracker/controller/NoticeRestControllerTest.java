package ru.smartup.timetracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.smartup.timetracker.core.SessionUserPrincipal;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.notice.request.NoticeDeleteDto;
import ru.smartup.timetracker.dto.notice.request.NoticeReadDto;
import ru.smartup.timetracker.dto.notice.response.NoticeDto;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.service.notification.NoticeScheduleService;
import ru.smartup.timetracker.service.notification.NoticeService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NoticeRestControllerTest {
    private static final long NOTICE_ID = 1;
    private static final int USER_ID = 1;
    private static final String USER_EMAIL = "user_email";

    private final NoticeService noticeService = Mockito.mock(NoticeService.class);
    private final NoticeScheduleService noticeScheduleService = mock(NoticeScheduleService.class);

    private NoticeRestController noticeRestController;

    @BeforeEach
    public void setUp() {
        noticeRestController = new NoticeRestController(noticeService, noticeScheduleService,
                new WebConfig().modelMapper());
    }

    @Test
    public void getNotices() {
        SessionUserPrincipal sessionUserPrincipal = createSessionUserPrincipal();

        when(noticeService.getNoticesByUserId(sessionUserPrincipal.getId())).thenReturn(List.of(createNotice()));

        List<NoticeDto> notices = noticeRestController.getNotices(sessionUserPrincipal);

        assertEquals(1, notices.size());
        assertEquals(NOTICE_ID, notices.get(0).getId());
    }

    @Test
    public void getNotice() {
        SessionUserPrincipal sessionUserPrincipal = createSessionUserPrincipal();

        when(noticeService.getNoticeByIdAndUserId(NOTICE_ID, USER_ID)).thenReturn(Optional.of(createNotice()));

        assertEquals(NOTICE_ID, noticeRestController.getNotice(sessionUserPrincipal, NOTICE_ID).getId());
    }

    @Test
    public void getNotice_shouldReturnResourceNotFoundException() {
        when(noticeService.getNoticeByIdAndUserId(NOTICE_ID, USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> noticeRestController.getNotice(createSessionUserPrincipal(), NOTICE_ID));
    }

    @Test
    public void readNoticesByIds() {
        NoticeReadDto noticeReadDto = new NoticeReadDto();
        noticeReadDto.setNoticeIds(Set.of(NOTICE_ID));

        noticeRestController.readNoticesByIds(createSessionUserPrincipal(), noticeReadDto);

        verify(noticeService).readNoticesByIdsAndUserId(noticeReadDto.getNoticeIds(), USER_ID);
    }

    @Test
    public void readAllNotices() {
        noticeRestController.readAllNotices(createSessionUserPrincipal());

        verify(noticeService).readAllNoticesByUserId(USER_ID);
    }

    @Test
    public void deleteNoticesByIds() {
        NoticeDeleteDto noticeDeleteDto = new NoticeDeleteDto();
        noticeDeleteDto.setNoticeIds(Set.of(NOTICE_ID));

        noticeRestController.deleteNoticesByIds(createSessionUserPrincipal(), noticeDeleteDto);

        verify(noticeService).deleteNoticesByIdsAndUserId(noticeDeleteDto.getNoticeIds(), USER_ID);
    }

    @Test
    public void deleteAllNotices() {
        noticeRestController.deleteAllNotices(createSessionUserPrincipal());

        verify(noticeService).deleteAllNoticesByUserId(USER_ID);
    }

    private SessionUserPrincipal createSessionUserPrincipal() {
        SessionUserPrincipal sessionUserPrincipal = new SessionUserPrincipal(USER_ID, USER_EMAIL);
        UserRole userRole = new UserRole();
        userRole.setUserId(USER_ID);
        userRole.setRoleId(UserRoleEnum.ROLE_USER);
        sessionUserPrincipal.setAllRoles(List.of(userRole), List.of());
        return sessionUserPrincipal;
    }

    private Notice createNotice() {
        Notice notice = new Notice();
        notice.setId(NOTICE_ID);
        notice.setUserId(USER_ID);
        return notice;
    }
}