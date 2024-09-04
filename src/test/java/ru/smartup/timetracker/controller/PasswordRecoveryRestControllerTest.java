package ru.smartup.timetracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.password.recovery.request.PasswordRecoveryDto;
import ru.smartup.timetracker.dto.password.recovery.request.PasswordResetDto;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;
import ru.smartup.timetracker.entity.PasswordResetToken;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.service.PasswordResetTokenService;
import ru.smartup.timetracker.service.UserService;
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
    private static final String USER_FIRST_NAME = "user_first_name";
    private static final String EMAIL = "user_email";
    private static final String PASSWORD = "password";
    private static final long PASSWORD_RECOVERY_TOKEN_TTL = 3600;
    private static final int USER_ID = 1;

    private final UserService userService = mock(UserService.class);
    private final PasswordResetTokenService passwordResetTokenService = mock(PasswordResetTokenService.class);
    private final NotifierObservable notifier = mock(NotifierObservable.class);
    private PasswordRecoveryRestController passwordRecoveryRestController;

    @BeforeEach
    public void setUp() {
        WebConfig webConfig = new WebConfig();
        passwordRecoveryRestController = new PasswordRecoveryRestController(
                userService, passwordResetTokenService, notifier);
    }

    @Test
    public void sendPasswordRecoveryLink_whenUserNotFound() {
        PasswordRecoveryDto passwordRecoveryDto = new PasswordRecoveryDto();
        passwordRecoveryDto.setEmail(EMAIL);
        when(userService.getNotArchivedUserByEmail(EMAIL)).thenReturn(Optional.empty());

        passwordRecoveryRestController.sendPasswordRecoveryLink(passwordRecoveryDto);

        verify(passwordResetTokenService, never()).createPasswordResetTokenForRecovery(anyInt());
        verify(notifier, never()).notifySpecificChannels(anyList(), any(Notice.class) ,anyString());
    }

    @Test
    public void sendPasswordRecoveryLink_whenUserFound() {
        Optional<User> user = createUser();
        PasswordRecoveryDto passwordRecoveryDto = new PasswordRecoveryDto();
        passwordRecoveryDto.setEmail(EMAIL);
        when(userService.getNotArchivedUserByEmail(EMAIL)).thenReturn(user);

        passwordRecoveryRestController.sendPasswordRecoveryLink(passwordRecoveryDto);

        verify(passwordResetTokenService).createPasswordResetTokenForRecovery(USER_ID);
        verify(notifier).notifyEmailChannel(eq(List.of(user.get())), any(Notice.class));
    }

    @Test
    public void resetPassword() {
        Optional<User> user = createUser();
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setToken(PASSWORD_RECOVERY_TOKEN);
        passwordResetDto.setNewPassword(PASSWORD);

        when(passwordResetTokenService.resetPassword(passwordResetDto)).thenReturn(user);

        passwordRecoveryRestController.resetPassword(passwordResetDto);
        final Notice notice = new Notice(NoticeTypeEnum.PASSWORD_UPDATE, "");

        verify(notifier).notifyEmailChannel(eq(List.of(user.get())), eq(notice));
    }

    private Optional<User> createUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setFirstName(USER_FIRST_NAME);
        user.setEmail(EMAIL);
        user.setArchived(false);
        return Optional.of(user);
    }

    private Optional<PasswordResetToken> createPasswordResetToken(Timestamp tokenExpiry) {
        PasswordResetToken token = new PasswordResetToken(USER_ID, PASSWORD_RECOVERY_TOKEN, tokenExpiry);
        return Optional.of(token);
    }
}