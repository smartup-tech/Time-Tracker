package ru.smartup.timetracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.notice.request.NoticeDeleteDto;
import ru.smartup.timetracker.dto.notice.request.NoticeReadDto;
import ru.smartup.timetracker.dto.notice.response.NoticeDto;
import ru.smartup.timetracker.entity.EmployeeRole;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.field.enumerated.EmployeeRoleEnum;
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
    private static final int EMPLOYEE_ID = 1;
    private static final String EMPLOYEE_EMAIL = "employee_email";

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
        SessionEmployeePrincipal sessionEmployeePrincipal = createSessionEmployeePrincipal();

        when(noticeService.getNoticesByEmployeeId(sessionEmployeePrincipal.getId())).thenReturn(List.of(createNotice()));

        List<NoticeDto> notices = noticeRestController.getNotices(sessionEmployeePrincipal);

        assertEquals(1, notices.size());
        assertEquals(NOTICE_ID, notices.get(0).getId());
    }

    @Test
    public void getNotice() {
        SessionEmployeePrincipal sessionEmployeePrincipal = createSessionEmployeePrincipal();

        when(noticeService.getNoticeByIdAndEmployeeId(NOTICE_ID, EMPLOYEE_ID)).thenReturn(Optional.of(createNotice()));

        assertEquals(NOTICE_ID, noticeRestController.getNotice(sessionEmployeePrincipal, NOTICE_ID).getId());
    }

    @Test
    public void getNotice_shouldReturnResourceNotFoundException() {
        when(noticeService.getNoticeByIdAndEmployeeId(NOTICE_ID, EMPLOYEE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> noticeRestController.getNotice(createSessionEmployeePrincipal(), NOTICE_ID));
    }

    @Test
    public void readNoticesByIds() {
        NoticeReadDto noticeReadDto = new NoticeReadDto();
        noticeReadDto.setNoticeIds(Set.of(NOTICE_ID));

        noticeRestController.readNoticesByIds(createSessionEmployeePrincipal(), noticeReadDto);

        verify(noticeService).readNoticesByIdsAndEmployeeId(noticeReadDto.getNoticeIds(), EMPLOYEE_ID);
    }

    @Test
    public void readAllNotices() {
        noticeRestController.readAllNotices(createSessionEmployeePrincipal());

        verify(noticeService).readAllNoticesByEmployeeId(EMPLOYEE_ID);
    }

    @Test
    public void deleteNoticesByIds() {
        NoticeDeleteDto noticeDeleteDto = new NoticeDeleteDto();
        noticeDeleteDto.setNoticeIds(Set.of(NOTICE_ID));

        noticeRestController.deleteNoticesByIds(createSessionEmployeePrincipal(), noticeDeleteDto);

        verify(noticeService).deleteNoticesByIdsAndEmployeeId(noticeDeleteDto.getNoticeIds(), EMPLOYEE_ID);
    }

    @Test
    public void deleteAllNotices() {
        noticeRestController.deleteAllNotices(createSessionEmployeePrincipal());

        verify(noticeService).deleteAllNoticesByEmployeeId(EMPLOYEE_ID);
    }

    private SessionEmployeePrincipal createSessionEmployeePrincipal() {
        SessionEmployeePrincipal sessionEmployeePrincipal = new SessionEmployeePrincipal(EMPLOYEE_ID, EMPLOYEE_EMAIL);
        EmployeeRole employeeRole = new EmployeeRole();
        employeeRole.setEmployeeId(EMPLOYEE_ID);
        employeeRole.setRoleId(EmployeeRoleEnum.ROLE_EMPLOYEE);
        sessionEmployeePrincipal.setAllRoles(List.of(employeeRole), List.of());
        return sessionEmployeePrincipal;
    }

    private Notice createNotice() {
        Notice notice = new Notice();
        notice.setId(NOTICE_ID);
        notice.setEmployeeId(EMPLOYEE_ID);
        return notice;
    }
}