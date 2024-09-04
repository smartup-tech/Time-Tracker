package ru.smartup.timetracker.dto.profile.request;

import lombok.Data;
import ru.smartup.timetracker.validation.ValidPassword;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PasswordUpdateDto {
    @NotBlank(message = "blank oldPassword")
    private String oldPassword;

    @NotNull
    @ValidPassword
    private String newPassword;
}
