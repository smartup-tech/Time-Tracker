package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.core.CurrentSessionUserPrincipal;
import ru.smartup.timetracker.core.SessionUserPrincipal;
import ru.smartup.timetracker.dto.freeze.request.FreezeDateDtoRequest;
import ru.smartup.timetracker.dto.freeze.response.FreezeRecordDto;
import ru.smartup.timetracker.exception.LockedException;
import ru.smartup.timetracker.service.freeze.CRUDFreezeService;
import ru.smartup.timetracker.service.freeze.FreezeService;
import ru.smartup.timetracker.utils.InitBinderUtils;

import java.time.LocalDate;
import java.util.List;

@PreAuthorize("getPrincipal().isAdmin()")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/freeze")
public class FreezeRecordRestController {
    private final CRUDFreezeService crudFreezeService;
    private final FreezeService freezeService;

    @InitBinder
    private void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(LocalDate.class, InitBinderUtils.getCustomLocalDateEditor());
    }

    @GetMapping
    public List<FreezeRecordDto> getFreezeRecordData() {
        return crudFreezeService.getFreezeRecordsDto();
    }

    @GetMapping("/last")
    public FreezeRecordDto getLastFreezeRecordData() {
        return crudFreezeService.getLastFreeze();
    }

    @PutMapping
    public List<FreezeRecordDto> updateFreezeData(final @CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
                                                  final @RequestBody FreezeDateDtoRequest dates) {
        if (freezeService.createOrUpdateTask(dates, currentSessionUserPrincipal.getId())) {
            return crudFreezeService.getFreezeRecordsDto();
        }
        throw new LockedException("Another operation already in progress. Please try again later.");
    }

    @PutMapping("/unfreeze")
    public FreezeRecordDto unfreezeLast(final @CurrentSessionUserPrincipal SessionUserPrincipal userPrincipal) {
        if(freezeService.unfreezeLastRecord()) {
            return crudFreezeService.getUnfreezeRecordDto();
        }
        throw new LockedException("Another operation already in progress. Please try again later.");
    }

    @GetMapping("/unfreeze")
    public FreezeRecordDto getUnfreezeRecord() {
        return crudFreezeService.getUnfreezeRecordDto();
    }
}
