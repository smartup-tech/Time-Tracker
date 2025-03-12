package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.core.CurrentSessionEmployeePrincipal;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.dto.notice.request.NoticeDeleteDto;
import ru.smartup.timetracker.dto.notice.request.NoticeReadDto;
import ru.smartup.timetracker.dto.notice.response.NoticeDto;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.service.notification.NoticeScheduleService;
import ru.smartup.timetracker.service.notification.NoticeService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notices")
public class NoticeRestController {
    private final NoticeService noticeService;
    private final NoticeScheduleService noticeScheduleService;

    private final ModelMapper modelMapper;

    @GetMapping
    public List<NoticeDto> getNotices(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal) {
        return noticeService.getNoticesByEmployeeId(currentSessionEmployeePrincipal.getId()).stream()
                .map(notice -> modelMapper.map(notice, NoticeDto.class))
                .collect(Collectors.toList());
    }

    @GetMapping("/unread")
    public int getNumberUnreadNotices(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal) {
        return noticeService.getNumberUnreadNotices(currentSessionEmployeePrincipal.getId());
    }

    @GetMapping("/{id}")
    public NoticeDto getNotice(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                               @Min(1) @PathVariable("id") long id) {
        Notice notice = noticeService.getNoticeByIdAndEmployeeId(id, currentSessionEmployeePrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notice was not found by id = " + id + "."));
        return modelMapper.map(notice, NoticeDto.class);
    }

    @PatchMapping
    public void readNoticesByIds(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                                 @Valid @RequestBody NoticeReadDto noticeReadDto) {
        noticeService.readNoticesByIdsAndEmployeeId(noticeReadDto.getNoticeIds(), currentSessionEmployeePrincipal.getId());
    }

    @PatchMapping("/all")
    public void readAllNotices(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal) {
        noticeService.readAllNoticesByEmployeeId(currentSessionEmployeePrincipal.getId());
    }

    @PutMapping
    public void deleteNoticesByIds(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                                   @Valid @RequestBody NoticeDeleteDto noticeDeleteDto) {
        noticeService.deleteNoticesByIdsAndEmployeeId(noticeDeleteDto.getNoticeIds(), currentSessionEmployeePrincipal.getId());
    }

    @DeleteMapping("/all")
    public void deleteAllNotices(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal) {
        noticeService.deleteAllNoticesByEmployeeId(currentSessionEmployeePrincipal.getId());
    }
}
