package ru.smartup.timetracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import ru.smartup.timetracker.core.SessionUserPrincipal;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.freeze.request.FreezeDateDtoRequest;
import ru.smartup.timetracker.dto.freeze.response.FreezeRecordDto;
import ru.smartup.timetracker.dto.user.response.UserShortDto;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.field.enumerated.FreezeRecordStatusEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.exception.LockedException;
import ru.smartup.timetracker.service.freeze.CRUDFreezeService;
import ru.smartup.timetracker.service.UserService;
import ru.smartup.timetracker.service.freeze.FreezeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FreezeRecordRestControllerTest {
    private static final int USER_ID = 5;
    private static final LocalDate FREEZE_DATE = LocalDate.parse("2022-01-31");

    private FreezeRecordRestController freezeRecordRestController;
    private final UserService userService = mock(UserService.class);
    private final CRUDFreezeService CRUDFreezeService = mock(CRUDFreezeService.class);
    private final FreezeService freezeService = mock(FreezeService.class);
    private ModelMapper modelMapper;
    @BeforeEach
    public void setUp() {
        modelMapper = new WebConfig().modelMapper();
        freezeRecordRestController = new FreezeRecordRestController(
                CRUDFreezeService, freezeService);
    }

    @Test
    public void getFreezeRecordData() {
        FreezeRecord freezeRecord = createFreezeRecord();
        UserShortDto userShortDto = modelMapper.map(freezeRecord.getUser(), UserShortDto.class);
        FreezeRecordDto freezeRecordDto = modelMapper.map(freezeRecord, FreezeRecordDto.class);
        freezeRecordDto.setUsers(userShortDto);
        List<FreezeRecordDto> freezeRecords = List.of(freezeRecordDto);

        when(CRUDFreezeService.getFreezeRecordsDto()).thenReturn(freezeRecords);
        when(userService.getUser(USER_ID)).thenReturn(createUser());

        List<FreezeRecordDto> freezeRecordData = freezeRecordRestController.getFreezeRecordData();

        assertEquals(1, freezeRecordData.size());
        assertEquals(USER_ID, freezeRecordData.get(0).getUsers().getId());
        assertEquals(FreezeRecordStatusEnum.WAITING, freezeRecordData.get(0).getStatus());
    }

    @Test
    public void updateFreezeData() {
        FreezeRecord freezeRecord = createFreezeRecord();
        UserShortDto userShortDto = modelMapper.map(freezeRecord.getUser(), UserShortDto.class);
        FreezeRecordDto freezeRecordDto = modelMapper.map(freezeRecord, FreezeRecordDto.class);
        freezeRecordDto.setUsers(userShortDto);
        List<FreezeRecordDto> freezeRecords = List.of(freezeRecordDto);

        FreezeDateDtoRequest freezeDateDtoRequest = new FreezeDateDtoRequest(List.of(FREEZE_DATE));

        when(CRUDFreezeService.getFreezeRecordsDto()).thenReturn(freezeRecords);
        when(userService.getUser(USER_ID)).thenReturn(createUser());
        when(freezeService.createOrUpdateTask(freezeDateDtoRequest, USER_ID)).thenReturn(true);

        SessionUserPrincipal currentSessionUserPrincipal = new SessionUserPrincipal(USER_ID, null);
        List<FreezeRecordDto> freezeRecordData = freezeRecordRestController.updateFreezeData(currentSessionUserPrincipal, freezeDateDtoRequest);

        assertEquals(1, freezeRecordData.size());
        assertEquals(USER_ID, freezeRecordData.get(0).getUsers().getId());
        assertEquals(FreezeRecordStatusEnum.WAITING, freezeRecordData.get(0).getStatus());
    }

    @Test
    public void updateFreezeData_shouldReturnException() {
        FreezeDateDtoRequest freezeDateDtoRequest = new FreezeDateDtoRequest(List.of(FREEZE_DATE));
        when(freezeService.createOrUpdateTask(freezeDateDtoRequest, USER_ID)).thenReturn(false);

        SessionUserPrincipal currentSessionUserPrincipal = new SessionUserPrincipal(USER_ID, null);

        assertThrows(LockedException.class,
                () -> freezeRecordRestController.updateFreezeData(currentSessionUserPrincipal, freezeDateDtoRequest));

    }

    private FreezeRecord createFreezeRecord() {
        FreezeRecord freezeRecord = new FreezeRecord();
        User user = new User();
        user.setId(USER_ID);
        freezeRecord.setUser(user);
        freezeRecord.setStatus(FreezeRecordStatusEnum.WAITING);
        return freezeRecord;
    }

    private Optional<User> createUser() {
        User user = new User();
        user.setId(USER_ID);
        return Optional.of(user);
    }
}