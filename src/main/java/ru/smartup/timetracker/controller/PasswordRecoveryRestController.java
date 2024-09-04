package ru.smartup.timetracker.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.smartup.timetracker.dto.password.recovery.request.PasswordRecoveryDto;
import ru.smartup.timetracker.dto.password.recovery.request.PasswordResetDto;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.pojo.notice.NoticePersonalToken;
import ru.smartup.timetracker.service.PasswordResetTokenService;
import ru.smartup.timetracker.service.UserService;
import ru.smartup.timetracker.service.notification.notifier.NotifierObservable;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/passwordRecovery")
public class PasswordRecoveryRestController {
    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final NotifierObservable notifierObservable;

    public PasswordRecoveryRestController(UserService userService,
                                          PasswordResetTokenService passwordResetTokenService,
                                          NotifierObservable notifierObservable) {
        this.userService = userService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.notifierObservable = notifierObservable;
    }

    @PostMapping("/sendLink")
    public void sendPasswordRecoveryLink(@Valid @RequestBody PasswordRecoveryDto passwordRecoveryDto) {
        String email = passwordRecoveryDto.getEmail();
        Optional<User> existUser = userService.getNotArchivedUserByEmail(email);

        existUser.ifPresent(user -> {
            final String token = passwordResetTokenService.createPasswordResetTokenForRecovery(user.getId());
            final long ttlInHours = passwordResetTokenService.getPasswordRecoveryTokenTtlInHours();

            final NoticePersonalToken noticePersonalToken = new NoticePersonalToken(user.getFirstName(), token, ttlInHours);
            final Notice notice = new Notice(NoticeTypeEnum.PASSWORD_RECOVERY, noticePersonalToken);

            notifierObservable.notifyEmailChannel(List.of(user), notice);
        });
    }

    @PostMapping("/resetPassword")
    public void resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDto) {
        Optional<User> existUser = passwordResetTokenService.resetPassword(passwordResetDto);

        existUser.ifPresent(user -> {

            final Notice notice = new Notice(NoticeTypeEnum.PASSWORD_UPDATE, "");

            notifierObservable.notifyEmailChannel(List.of(user), notice);
        });
    }
}
