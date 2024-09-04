package ru.smartup.timetracker.dto.user.request;

import lombok.Data;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.validation.OptionalField;
import ru.smartup.timetracker.validation.RequiredField;
import ru.smartup.timetracker.validation.ValidPassword;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class UserUpdateDto {
    @RequiredField(maxSize = 255)
    private String firstName;

    @OptionalField(maxSize = 255)
    private String middleName;

    @RequiredField(maxSize = 255)
    private String lastName;

    @NotEmpty
    @Email(message = "invalid email")
    private String email;

    @ValidPassword
    private String password;

    @Min(1)
    private int positionId;

    //TODO желательно заменить либо на другой enum, либо на строку, так как UserRoleEnum - базючный
    @NotEmpty
    private List<UserRoleEnum> roles;
}
