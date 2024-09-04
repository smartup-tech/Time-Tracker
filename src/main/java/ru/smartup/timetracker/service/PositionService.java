package ru.smartup.timetracker.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smartup.timetracker.core.CacheNames;
import ru.smartup.timetracker.dto.PageableRequestParamDto;
import ru.smartup.timetracker.dto.QueryArchiveParamRequestDto;
import ru.smartup.timetracker.dto.position.response.PositionDto;
import ru.smartup.timetracker.entity.Position;
import ru.smartup.timetracker.repository.PositionRepository;
import ru.smartup.timetracker.repository.criteria.PositionFilterBuilder;
import ru.smartup.timetracker.utils.PageableMaker;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PositionService {
    private final PositionRepository positionRepository;
    private final PageableMaker pageableMaker;
    private final ModelMapper modelMapper;

    public Page<PositionDto> getPositions(final QueryArchiveParamRequestDto queryArchiveParam, final PageableRequestParamDto pageableParam) {

        Page<Position> positions = getPageableAndFilteredPositions(queryArchiveParam, pageableParam);

        return positions.map(position -> modelMapper.map(position, PositionDto.class));
    }

    private Page<Position> getPageableAndFilteredPositions(final QueryArchiveParamRequestDto queryArchiveParam, final PageableRequestParamDto pageableParam) {
        Pageable pageable = pageableMaker.make(pageableParam);
        Specification<Position> positionSpec = getPositionFilters(queryArchiveParam.getQuery(), queryArchiveParam.isArchive());
        return getPositions(positionSpec, pageable);
    }

    private Specification<Position> getPositionFilters(final String searchValue, final boolean archive) {
        PositionFilterBuilder builder = new PositionFilterBuilder();

        builder.addIsArchiveFilter(archive);

        if (!searchValue.isBlank()) {
            builder.addNameFilter(searchValue);
        }

        return builder.buildSpecification();
    }

    @Cacheable(cacheNames = CacheNames.Position.GET_POSITIONS_SEARCH_PAGEABLE)
    public Page<Position> getPositions(final Specification<Position> filters, final Pageable pageable) {
        return positionRepository.findAll(filters, pageable);
    }

    @Cacheable(cacheNames = CacheNames.Position.GET_ACTIVE_POSITIONS)
    public List<Position> getActivePositions() {
        return positionRepository.findAllByIsArchivedFalseOrderByName();
    }

    @Cacheable(cacheNames = CacheNames.Position.GET_POSITION_BY_ID)
    public Optional<Position> getPosition(int positionId) {
        return positionRepository.findById(positionId);
    }

    @Cacheable(cacheNames = CacheNames.Position.GET_NOT_ARCHIVED_POSITION_BY_ID)
    public Optional<Position> getNotArchivedPosition(int positionId) {
        return positionRepository.findByIdAndIsArchivedFalse(positionId);
    }

    public boolean isNotUnique(String positionName) {
        return positionRepository.isNotUnique(positionName);
    }

    public boolean isNotUnique(int positionId, String positionName) {
        return positionRepository.isNotUnique(positionId, positionName);
    }

    @CacheEvict(value = {
            CacheNames.Position.GET_ACTIVE_POSITIONS,
            CacheNames.Position.GET_POSITIONS_SEARCH_PAGEABLE,
            CacheNames.Position.GET_POSITION_BY_ID,
            CacheNames.Position.GET_NOT_ARCHIVED_POSITION_BY_ID},
            allEntries = true)
    @Transactional
    public void createPosition(Position position) {
        positionRepository.save(position);
    }

    @CacheEvict(value = {
            CacheNames.Position.GET_ACTIVE_POSITIONS,
            CacheNames.Position.GET_POSITIONS_SEARCH_PAGEABLE,
            CacheNames.Position.GET_POSITION_BY_ID,
            CacheNames.Position.GET_NOT_ARCHIVED_POSITION_BY_ID},
            allEntries = true)
    @Transactional
    public void updatePosition(Position position) {
        positionRepository.save(position);
    }

    @CacheEvict(value = {
            CacheNames.Position.GET_ACTIVE_POSITIONS,
            CacheNames.Position.GET_POSITIONS_SEARCH_PAGEABLE,
            CacheNames.Position.GET_POSITION_BY_ID,
            CacheNames.Position.GET_NOT_ARCHIVED_POSITION_BY_ID},
            allEntries = true)
    @Transactional
    public void archivePosition(int positionId) {
        positionRepository.archivePosition(positionId);
    }
}
