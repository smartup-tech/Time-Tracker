package ru.smartup.timetracker.dto.password.recovery.request;

import lombok.Data;
import ru.smartup.timetracker.validation.ValidPassword;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PasswordResetDto {
    @NotBlank
    private String token;

    @NotNull
    @ValidPassword
    private String newPassword;
}
