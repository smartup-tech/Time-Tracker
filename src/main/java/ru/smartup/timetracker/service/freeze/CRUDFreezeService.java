package ru.smartup.timetracker.service.freeze;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smartup.timetracker.core.CacheNames;
import ru.smartup.timetracker.dto.freeze.response.FreezeRecordDto;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.field.enumerated.FreezeRecordStatusEnum;
import ru.smartup.timetracker.repository.FreezeRecordRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CRUDFreezeService {
    private final FreezeRecordRepository freezeRecordRepository;
    private final ModelMapper modelMapper;

    @CacheEvict(value = {CacheNames.FreezeRecord.GET_RECORDS, CacheNames.FreezeRecord.GET_WITH_MAX_FREEZE_DATE},
            allEntries = true)
    @Transactional
    public void save(FreezeRecord freezeRecord) {
        freezeRecordRepository.save(freezeRecord);
    }

    @Transactional
    @CacheEvict(value = {CacheNames.FreezeRecord.GET_RECORDS, CacheNames.FreezeRecord.GET_WITH_MAX_FREEZE_DATE},
            allEntries = true)
    public void setNewFreezeSchedule(final List<FreezeRecord> newScheduleRecord, final List<FreezeRecord> oldScheduleRecord) {
        freezeRecordRepository.saveAll(newScheduleRecord);
        freezeRecordRepository.deleteAllInBatch(oldScheduleRecord);
    }

    public List<FreezeRecordDto> getFreezeRecordsDto() {
        return getFreezeRecords()
                .stream()
                .map(freezeRecord -> modelMapper.map(freezeRecord, FreezeRecordDto.class))
                .collect(Collectors.toList());
    }

    public List<FreezeRecord> getFreezeRecords() {
        return freezeRecordRepository.findAllAfterCompleted();
    }

    public FreezeRecordDto getUnfreezeRecordDto() {
        FreezeRecord freezeRecord = getUnfreezeRecord();
        return freezeRecord == null ? null : modelMapper.map(
                freezeRecord,
                FreezeRecordDto.class
        );
    }

    public FreezeRecord getUnfreezeRecord() {
        return freezeRecordRepository.findFirstByStatusOrderByFreezeDateDesc(FreezeRecordStatusEnum.UN_FREEZE);
    }

    public FreezeRecordDto getLastFreeze() {
        FreezeRecord freezeRecord = getCacheableLastFreeze();
        return freezeRecord == null ? null : modelMapper.map(
                freezeRecord,
                FreezeRecordDto.class
        );
    }

    @Cacheable(cacheNames = CacheNames.FreezeRecord.GET_WITH_MAX_FREEZE_DATE)
    public FreezeRecord getCacheableLastFreeze() {
        return freezeRecordRepository.findFirstByStatusOrderByFreezeDateDesc(FreezeRecordStatusEnum.COMPLETED);
    }

    public FreezeRecord getFreezeWithMaxDateByStatus(FreezeRecordStatusEnum statusEnum) {
        return freezeRecordRepository.findFirstByStatusOrderByFreezeDateDesc(statusEnum);
    }

    public FreezeRecord getFreezeWithMinDateByStatus(FreezeRecordStatusEnum statusEnum) {
        return freezeRecordRepository.findFirstByStatusOrderByFreezeDateAsc(statusEnum);
    }


    public List<LocalDate> getBoundaryFreezeRecord(final FreezeRecord freezeRecord) {
        return freezeRecordRepository.findBoundaryByFreezeDate(freezeRecord.getFreezeDate())
                .stream()
                .map(date -> date == null ? null : date.toLocalDate())
                .collect(Collectors.toList());
    }

    public void deleteFreezeRecord(final FreezeRecord freezeRecord) {
        freezeRecordRepository.delete(freezeRecord);
    }
}
