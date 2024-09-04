package ru.smartup.timetracker.dto.project.response;

import lombok.*;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;

@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectOfUserDto extends ProjectShortDto {
    private Float externalRate;

    private ProjectRoleEnum projectRoleId;
}
