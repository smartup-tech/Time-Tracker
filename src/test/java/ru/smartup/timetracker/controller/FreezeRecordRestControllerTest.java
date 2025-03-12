package ru.smartup.timetracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.employee.response.EmployeeShortDto;
import ru.smartup.timetracker.dto.freeze.request.FreezeDateDtoRequest;
import ru.smartup.timetracker.dto.freeze.response.FreezeRecordDto;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.field.enumerated.FreezeRecordStatusEnum;
import ru.smartup.timetracker.exception.LockedException;
import ru.smartup.timetracker.service.EmployeeService;
import ru.smartup.timetracker.service.freeze.CRUDFreezeService;
import ru.smartup.timetracker.service.freeze.FreezeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FreezeRecordRestControllerTest {
    private static final int EMPLOYEE_ID = 5;
    private static final LocalDate FREEZE_DATE = LocalDate.parse("2022-01-31");

    private FreezeRecordRestController freezeRecordRestController;
    private final EmployeeService employeeService = mock(EmployeeService.class);
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
        EmployeeShortDto employeeShortDto = modelMapper.map(freezeRecord.getEmployee(), EmployeeShortDto.class);
        FreezeRecordDto freezeRecordDto = modelMapper.map(freezeRecord, FreezeRecordDto.class);
        freezeRecordDto.setEmployees(employeeShortDto);
        List<FreezeRecordDto> freezeRecords = List.of(freezeRecordDto);

        when(CRUDFreezeService.getFreezeRecordsDto()).thenReturn(freezeRecords);
        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(createEmployee());

        List<FreezeRecordDto> freezeRecordData = freezeRecordRestController.getFreezeRecordData();

        assertEquals(1, freezeRecordData.size());
        assertEquals(EMPLOYEE_ID, freezeRecordData.get(0).getEmployees().getId());
        assertEquals(FreezeRecordStatusEnum.WAITING, freezeRecordData.get(0).getStatus());
    }

    @Test
    public void updateFreezeData() {
        FreezeRecord freezeRecord = createFreezeRecord();
        EmployeeShortDto employeeShortDto = modelMapper.map(freezeRecord.getEmployee(), EmployeeShortDto.class);
        FreezeRecordDto freezeRecordDto = modelMapper.map(freezeRecord, FreezeRecordDto.class);
        freezeRecordDto.setEmployees(employeeShortDto);
        List<FreezeRecordDto> freezeRecords = List.of(freezeRecordDto);

        FreezeDateDtoRequest freezeDateDtoRequest = new FreezeDateDtoRequest(List.of(FREEZE_DATE));

        when(CRUDFreezeService.getFreezeRecordsDto()).thenReturn(freezeRecords);
        when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(createEmployee());
        when(freezeService.createOrUpdateTask(freezeDateDtoRequest, EMPLOYEE_ID)).thenReturn(true);

        SessionEmployeePrincipal currentSessionEmployeePrincipal = new SessionEmployeePrincipal(EMPLOYEE_ID, null);
        List<FreezeRecordDto> freezeRecordData = freezeRecordRestController.updateFreezeData(currentSessionEmployeePrincipal, freezeDateDtoRequest);

        assertEquals(1, freezeRecordData.size());
        assertEquals(EMPLOYEE_ID, freezeRecordData.get(0).getEmployees().getId());
        assertEquals(FreezeRecordStatusEnum.WAITING, freezeRecordData.get(0).getStatus());
    }

    @Test
    public void updateFreezeData_shouldReturnException() {
        FreezeDateDtoRequest freezeDateDtoRequest = new FreezeDateDtoRequest(List.of(FREEZE_DATE));
        when(freezeService.createOrUpdateTask(freezeDateDtoRequest, EMPLOYEE_ID)).thenReturn(false);

        SessionEmployeePrincipal currentSessionEmployeePrincipal = new SessionEmployeePrincipal(EMPLOYEE_ID, null);

        assertThrows(LockedException.class,
                () -> freezeRecordRestController.updateFreezeData(currentSessionEmployeePrincipal, freezeDateDtoRequest));

    }

    private FreezeRecord createFreezeRecord() {
        FreezeRecord freezeRecord = new FreezeRecord();
        Employee employee = new Employee();
        employee.setId(EMPLOYEE_ID);
        freezeRecord.setEmployee(employee);
        freezeRecord.setStatus(FreezeRecordStatusEnum.WAITING);
        return freezeRecord;
    }

    private Optional<Employee> createEmployee() {
        Employee employee = new Employee();
        employee.setId(EMPLOYEE_ID);
        return Optional.of(employee);
    }
}