package ru.smartup.timetracker.dto.user.response;

import lombok.Data;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;

import java.util.List;

@Data
public class UserShortDto {
    private int id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String email;

    private boolean isArchived;

    private List<UserRoleEnum> roles = List.of();
}
