package ru.smartup.timetracker.dto.password.recovery.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
public class PasswordRecoveryDto {
    @NotEmpty
    @Email(message = "invalid email")
    private String email;
}
