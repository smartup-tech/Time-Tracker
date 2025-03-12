package ru.smartup.timetracker.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.smartup.timetracker.dto.password.recovery.request.PasswordRecoveryDto;
import ru.smartup.timetracker.dto.password.recovery.request.PasswordResetDto;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;
import ru.smartup.timetracker.pojo.notice.NoticePersonalToken;
import ru.smartup.timetracker.service.EmployeeService;
import ru.smartup.timetracker.service.PasswordResetTokenService;
import ru.smartup.timetracker.service.notification.notifier.NotifierObservable;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/passwordRecovery")
public class PasswordRecoveryRestController {
    private final EmployeeService employeeService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final NotifierObservable notifierObservable;

    public PasswordRecoveryRestController(EmployeeService employeeService,
                                          PasswordResetTokenService passwordResetTokenService,
                                          NotifierObservable notifierObservable) {
        this.employeeService = employeeService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.notifierObservable = notifierObservable;
    }

    @PostMapping("/sendLink")
    public void sendPasswordRecoveryLink(@Valid @RequestBody PasswordRecoveryDto passwordRecoveryDto) {
        String email = passwordRecoveryDto.getEmail();
        Optional<Employee> existEmployee = employeeService.getNotArchivedEmployeeByEmail(email);

        existEmployee.ifPresent(employee -> {
            final String token = passwordResetTokenService.createPasswordResetTokenForRecovery(employee.getId());
            final long ttlInHours = passwordResetTokenService.getPasswordRecoveryTokenTtlInHours();

            final NoticePersonalToken noticePersonalToken = new NoticePersonalToken(employee.getFirstName(), token, ttlInHours);
            final Notice notice = new Notice(NoticeTypeEnum.PASSWORD_RECOVERY, noticePersonalToken);

            notifierObservable.notifyEmailChannel(List.of(employee), notice);
        });
    }

    @PostMapping("/resetPassword")
    public void resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDto) {
        Optional<Employee> existEmployee = passwordResetTokenService.resetPassword(passwordResetDto);

        existEmployee.ifPresent(employee -> {

            final Notice notice = new Notice(NoticeTypeEnum.PASSWORD_UPDATE, "");

            notifierObservable.notifyEmailChannel(List.of(employee), notice);
        });
    }
}
