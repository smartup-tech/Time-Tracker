package ru.smartup.timetracker.dto.user.request.param;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class UserParamRequestDto {
    private String query = "";
    private boolean archive = false;
}
