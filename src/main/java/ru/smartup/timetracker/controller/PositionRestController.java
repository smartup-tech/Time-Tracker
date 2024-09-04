package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.dto.ErrorCode;
import ru.smartup.timetracker.dto.PageableRequestParamDto;
import ru.smartup.timetracker.dto.QueryArchiveParamRequestDto;
import ru.smartup.timetracker.dto.position.request.PositionCreateDto;
import ru.smartup.timetracker.dto.position.response.PositionDto;
import ru.smartup.timetracker.dto.user.response.UserShortDto;
import ru.smartup.timetracker.entity.Position;
import ru.smartup.timetracker.entity.field.sort.PositionSortFieldEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.field.sort.ProjectSortFieldEnum;
import ru.smartup.timetracker.exception.NotUniqueDataException;
import ru.smartup.timetracker.exception.RelatedEntitiesFoundException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.service.PositionService;
import ru.smartup.timetracker.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/positions")
public class PositionRestController {

    private final PositionService positionService;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final ConversionService conversionService;

    @PreAuthorize("getPrincipal().isAdmin()")
    @GetMapping
    public Page<PositionDto> getPositionsByPage(final @Valid QueryArchiveParamRequestDto positionRequestParam,
                                                final @Valid PageableRequestParamDto<PositionSortFieldEnum> pageableRequestParam) {
        pageableRequestParam.setSortBy(conversionService.convert(pageableRequestParam.getSortBy(), PositionSortFieldEnum.class));
        return positionService.getPositions(positionRequestParam, pageableRequestParam);
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @GetMapping("/active")
    public List<PositionDto> getActivePositions() {
        return positionService.getActivePositions().stream()
                .map(position -> modelMapper.map(position, PositionDto.class)).collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @GetMapping("/{positionId}")
    public PositionDto getPosition(@PathVariable("positionId") int positionId) {
        Optional<Position> existPosition = positionService.getPosition(positionId);
        if (existPosition.isEmpty()) {
            throw new ResourceNotFoundException("Position was not found by positionId = " + positionId + ".");
        }
        return modelMapper.map(existPosition.get(), PositionDto.class);
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @PostMapping
    public void createPosition(@Valid @RequestBody PositionCreateDto positionCreateDto) {
        if (positionService.isNotUnique(positionCreateDto.getName())) {
            throw new NotUniqueDataException(ErrorCode.NOT_UNIQUE_POSITION_NAME, "Position with specified name = '"
                    + positionCreateDto.getName() + "' already exists.");
        }
        Position position = modelMapper.map(positionCreateDto, Position.class);
        positionService.createPosition(position);
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @PatchMapping("/{positionId}")
    public void updatePosition(@Valid @RequestBody PositionCreateDto positionCreateDto,
                                    @PathVariable("positionId") int positionId) {
        Optional<Position> existPosition = positionService.getNotArchivedPosition(positionId);
        if (existPosition.isEmpty()) {
            throw new ResourceNotFoundException("Active position was not found by positionId = " + positionId + ".");
        }
        if (positionService.isNotUnique(positionId, positionCreateDto.getName())) {
            throw new NotUniqueDataException(ErrorCode.NOT_UNIQUE_POSITION_NAME, "Position with specified name = '"
                    + positionCreateDto.getName() + "' already exists.");
        }
        Position position = existPosition.get();
        modelMapper.map(positionCreateDto, position);
        positionService.updatePosition(position);
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @PostMapping("/{positionId}/archive")
    public void archivePosition(@PathVariable("positionId") int positionId) {
        Optional<Position> existPosition = positionService.getNotArchivedPosition(positionId);
        if (existPosition.isEmpty()) {
            throw new ResourceNotFoundException("Active position was not found by positionId = " + positionId + ".");
        }
        List<User> users = userService.getNotArchivedUsersWithPosition(positionId);
        if (!CollectionUtils.isEmpty(users)) {
            List<UserShortDto> linkedUsers = users.stream()
                    .map(user -> modelMapper.map(user, UserShortDto.class))
                    .collect(Collectors.toList());
            throw new RelatedEntitiesFoundException(ErrorCode.RELATED_ENTITIES_FOUND_FOR_POSITION,
                    "Archive is not available now. Please, check all users with specified position; positionId = "
                            + positionId + ".", linkedUsers);
        }
        positionService.archivePosition(positionId);
    }
}
