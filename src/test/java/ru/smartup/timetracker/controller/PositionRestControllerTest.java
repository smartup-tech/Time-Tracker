package ru.smartup.timetracker.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.PageableRequestParamDto;
import ru.smartup.timetracker.dto.QueryArchiveParamRequestDto;
import ru.smartup.timetracker.dto.position.request.PositionCreateDto;
import ru.smartup.timetracker.dto.position.response.PositionDto;
import ru.smartup.timetracker.entity.Position;
import ru.smartup.timetracker.entity.field.sort.PositionSortFieldEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.field.sort.PositionSortFieldEnum;
import ru.smartup.timetracker.exception.NotUniqueDataException;
import ru.smartup.timetracker.exception.RelatedEntitiesFoundException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.service.PositionService;
import ru.smartup.timetracker.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PositionRestControllerTest {
    private static final int PAGE = 1;
    private static final int SIZE = 10;
    private static final int POSITION_ID = 1;
    private static final String POSITION_NAME = "position_name";

    private PositionRestController positionRestController;
    private final PositionService positionService = mock(PositionService.class);
    private final UserService userService = mock(UserService.class);
    private final ConversionService conversionService = mock(ConversionService.class);
    private ModelMapper modelMapper;

    @BeforeEach
    public void setUp() {
        modelMapper = new WebConfig().modelMapper();
        positionRestController = new PositionRestController(positionService, userService, modelMapper, conversionService);
    }

    @Test
    public void getPositionsByPage() {
        Page<PositionDto> positions = new PageImpl<>(Stream.of(createPositionObj()).map(pos -> modelMapper.map(pos, PositionDto.class)).collect(Collectors.toList()));

        QueryArchiveParamRequestDto positionParam = createPositionParam("", false);
        PageableRequestParamDto pageableParam = createPageableParam(PAGE, SIZE, PositionSortFieldEnum.NAME, Sort.Direction.ASC);

        when(positionService.getPositions(positionParam, pageableParam)).thenReturn(positions);

        Page<PositionDto> positionsByPage = positionRestController
                .getPositionsByPage(positionParam, pageableParam);

        assertEquals(1, positionsByPage.getTotalElements());
    }

    @Test
    public void getPositionsByPage_whenSearchQuery() {
        Page<PositionDto> positions = new PageImpl<>(Stream.of(createPositionObj()).map(pos -> modelMapper.map(pos, PositionDto.class)).collect(Collectors.toList()));

        QueryArchiveParamRequestDto positionParam = createPositionParam(POSITION_NAME, false);
        PageableRequestParamDto pageableParam = createPageableParam(PAGE, SIZE, PositionSortFieldEnum.NAME, Sort.Direction.ASC);

        when(positionService.getPositions(positionParam, pageableParam)).thenReturn(positions);

        Page<PositionDto> positionsByPage = positionRestController
                .getPositionsByPage(positionParam, pageableParam);

        assertEquals(1, positionsByPage.getTotalElements());
    }

    @Test
    public void getPosition() {
        when(positionService.getPosition(POSITION_ID)).thenReturn(Optional.of(createPositionObj()));

        PositionDto positionDto = positionRestController.getPosition(POSITION_ID);

        assertEquals(POSITION_ID, positionDto.getId());
        assertEquals(POSITION_NAME, positionDto.getName());
    }

    @Test
    public void getPosition_shouldReturnException() {
        when(positionService.getPosition(POSITION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> positionRestController.getPosition(POSITION_ID));
    }

    @Test
    public void createPosition() {
        PositionCreateDto positionCreateDto = new PositionCreateDto();
        positionCreateDto.setName(POSITION_NAME);

        when(positionService.isNotUnique(POSITION_NAME)).thenReturn(false);

        positionRestController.createPosition(positionCreateDto);

        verify(positionService).createPosition(argThat(position -> position.getName().equals(positionCreateDto.getName())));
    }

    @Test
    public void createPosition_shouldReturnException() {
        PositionCreateDto positionCreateDto = new PositionCreateDto();
        positionCreateDto.setName(POSITION_NAME);

        when(positionService.isNotUnique(POSITION_NAME)).thenReturn(true);

        assertThrows(NotUniqueDataException.class, () -> positionRestController.createPosition(positionCreateDto));
    }

    @Test
    public void updatePosition() {
        Position position = createPositionObj();
        PositionCreateDto positionCreateDto = new PositionCreateDto();
        positionCreateDto.setName(POSITION_NAME);

        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.of(position));
        when(positionService.isNotUnique(POSITION_ID, POSITION_NAME)).thenReturn(false);

        positionRestController.updatePosition(positionCreateDto, POSITION_ID);

        verify(positionService).updatePosition(position);
    }

    @Test
    public void updatePosition_shouldReturnResourceNotFoundException() {
        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> positionRestController.updatePosition(any(), POSITION_ID));
    }

    @Test
    public void updatePosition_shouldReturnNotUniqueDataException() {
        PositionCreateDto positionCreateDto = new PositionCreateDto();
        positionCreateDto.setName(POSITION_NAME);

        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.of(createPositionObj()));
        when(positionService.isNotUnique(POSITION_ID, POSITION_NAME)).thenReturn(true);

        assertThrows(NotUniqueDataException.class, () -> positionRestController.updatePosition(positionCreateDto, POSITION_ID));
    }

    @Test
    public void archivePosition() {
        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.of(createPositionObj()));
        when(userService.getNotArchivedUsersWithPosition(POSITION_ID)).thenReturn(List.of());

        positionRestController.archivePosition(POSITION_ID);

        verify(positionService).archivePosition(POSITION_ID);
    }

    @Test
    public void archivePosition_shouldReturnResourceNotFoundException() {
        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> positionRestController.archivePosition(POSITION_ID));
    }

    @Test
    public void archivePosition_shouldReturnRelatedEntitiesFoundException() {
        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.of(createPositionObj()));
        when(userService.getNotArchivedUsersWithPosition(POSITION_ID)).thenReturn(List.of(new User()));

        assertThrows(RelatedEntitiesFoundException.class, () -> positionRestController.archivePosition(POSITION_ID));
    }

    private Position createPositionObj() {
        Position position = new Position();
        position.setId(POSITION_ID);
        position.setName(POSITION_NAME);
        return position;
    }

    private QueryArchiveParamRequestDto createPositionParam(String query, boolean archive) {
        QueryArchiveParamRequestDto paramRequest = new QueryArchiveParamRequestDto();
        paramRequest.setQuery(query);
        paramRequest.setArchive(archive);
        return paramRequest;
    }

    private PageableRequestParamDto createPageableParam(int page, int size, PositionSortFieldEnum sortBy, Sort.Direction sortDirection) {
        PageableRequestParamDto pageableRequestParamDto = new PageableRequestParamDto();
        pageableRequestParamDto.setPage(page);
        pageableRequestParamDto.setSize(size);
        pageableRequestParamDto.setSortBy(sortBy);
        pageableRequestParamDto.setSortDirection(sortDirection);
        return pageableRequestParamDto;
    }
}