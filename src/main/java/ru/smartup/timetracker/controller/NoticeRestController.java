package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.core.CurrentSessionUserPrincipal;
import ru.smartup.timetracker.core.SessionUserPrincipal;
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
    public List<NoticeDto> getNotices(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal) {
        return noticeService.getNoticesByUserId(currentSessionUserPrincipal.getId()).stream()
                .map(notice -> modelMapper.map(notice, NoticeDto.class))
                .collect(Collectors.toList());
    }

    @GetMapping("/unread")
    public int getNumberUnreadNotices(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal) {
        return noticeService.getNumberUnreadNotices(currentSessionUserPrincipal.getId());
    }

    @GetMapping("/{id}")
    public NoticeDto getNotice(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
                               @Min(1) @PathVariable("id") long id) {
        Notice notice = noticeService.getNoticeByIdAndUserId(id, currentSessionUserPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notice was not found by id = " + id + "."));
        return modelMapper.map(notice, NoticeDto.class);
    }

    @PatchMapping
    public void readNoticesByIds(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
                                 @Valid @RequestBody NoticeReadDto noticeReadDto) {
        noticeService.readNoticesByIdsAndUserId(noticeReadDto.getNoticeIds(), currentSessionUserPrincipal.getId());
    }

    @PatchMapping("/all")
    public void readAllNotices(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal) {
        noticeService.readAllNoticesByUserId(currentSessionUserPrincipal.getId());
    }

    @PutMapping
    public void deleteNoticesByIds(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
                                   @Valid @RequestBody NoticeDeleteDto noticeDeleteDto) {
        noticeService.deleteNoticesByIdsAndUserId(noticeDeleteDto.getNoticeIds(), currentSessionUserPrincipal.getId());
    }

    @DeleteMapping("/all")
    public void deleteAllNotices(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal) {
        noticeService.deleteAllNoticesByUserId(currentSessionUserPrincipal.getId());
    }
}
