package ru.smartup.timetracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.password.recovery.request.PasswordRecoveryDto;
import ru.smartup.timetracker.dto.password.recovery.request.PasswordResetDto;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.PasswordResetToken;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;
import ru.smartup.timetracker.service.EmployeeService;
import ru.smartup.timetracker.service.PasswordResetTokenService;
import ru.smartup.timetracker.service.notification.notifier.NotifierObservable;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PasswordRecoveryRestControllerTest {
    private static final Timestamp PASSWORD_RECOVERY_TOKEN_EXPIRY = Timestamp.from(Instant.ofEpochSecond(1672509600));
    private static final String PASSWORD_RECOVERY_TOKEN = "aaaaabbbbb";
    private static final String EMPLOYEE_FIRST_NAME = "employee_first_name";
    private static final String EMAIL = "employee_email";
    private static final String PASSWORD = "password";
    private static final long PASSWORD_RECOVERY_TOKEN_TTL = 3600;
    private static final int EMPLOYEE_ID = 1;

    private final EmployeeService employeeService = mock(EmployeeService.class);
    private final PasswordResetTokenService passwordResetTokenService = mock(PasswordResetTokenService.class);
    private final NotifierObservable notifier = mock(NotifierObservable.class);
    private PasswordRecoveryRestController passwordRecoveryRestController;

    @BeforeEach
    public void setUp() {
        WebConfig webConfig = new WebConfig();
        passwordRecoveryRestController = new PasswordRecoveryRestController(
                employeeService, passwordResetTokenService, notifier);
    }

    @Test
    public void sendPasswordRecoveryLink_whenEmployeeNotFound() {
        PasswordRecoveryDto passwordRecoveryDto = new PasswordRecoveryDto();
        passwordRecoveryDto.setEmail(EMAIL);
        when(employeeService.getNotArchivedEmployeeByEmail(EMAIL)).thenReturn(Optional.empty());

        passwordRecoveryRestController.sendPasswordRecoveryLink(passwordRecoveryDto);

        verify(passwordResetTokenService, never()).createPasswordResetTokenForRecovery(anyInt());
        verify(notifier, never()).notifySpecificChannels(anyList(), any(Notice.class) ,anyString());
    }

    @Test
    public void sendPasswordRecoveryLink_whenEmployeeFound() {
        Optional<Employee> employee = createEmployee();
        PasswordRecoveryDto passwordRecoveryDto = new PasswordRecoveryDto();
        passwordRecoveryDto.setEmail(EMAIL);
        when(employeeService.getNotArchivedEmployeeByEmail(EMAIL)).thenReturn(employee);

        passwordRecoveryRestController.sendPasswordRecoveryLink(passwordRecoveryDto);

        verify(passwordResetTokenService).createPasswordResetTokenForRecovery(EMPLOYEE_ID);
        verify(notifier).notifyEmailChannel(eq(List.of(employee.get())), any(Notice.class));
    }

    @Test
    public void resetPassword() {
        Optional<Employee> employee = createEmployee();
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setToken(PASSWORD_RECOVERY_TOKEN);
        passwordResetDto.setNewPassword(PASSWORD);

        when(passwordResetTokenService.resetPassword(passwordResetDto)).thenReturn(employee);

        passwordRecoveryRestController.resetPassword(passwordResetDto);
        final Notice notice = new Notice(NoticeTypeEnum.PASSWORD_UPDATE, "");

        verify(notifier).notifyEmailChannel(eq(List.of(employee.get())), eq(notice));
    }

    private Optional<Employee> createEmployee() {
        Employee employee = new Employee();
        employee.setId(EMPLOYEE_ID);
        employee.setFirstName(EMPLOYEE_FIRST_NAME);
        employee.setEmail(EMAIL);
        employee.setArchived(false);
        return Optional.of(employee);
    }

    private Optional<PasswordResetToken> createPasswordResetToken(Timestamp tokenExpiry) {
        PasswordResetToken token = new PasswordResetToken(EMPLOYEE_ID, PASSWORD_RECOVERY_TOKEN, tokenExpiry);
        return Optional.of(token);
    }
}