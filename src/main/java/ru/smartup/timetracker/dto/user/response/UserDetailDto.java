package ru.smartup.timetracker.dto.user.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.smartup.timetracker.dto.position.response.PositionDto;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserDetailDto extends UserShortDto {
    private PositionDto position;

    private Map<Integer, List<ProjectRoleEnum>> projectRoles = Map.of();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp lastModifiedDate;
}
