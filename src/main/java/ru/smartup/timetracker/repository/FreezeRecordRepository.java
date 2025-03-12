package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.field.enumerated.FreezeRecordStatusEnum;
import ru.smartup.timetracker.pojo.freeze.UnfreezeDateInterval;

import java.time.LocalDate;
import java.util.List;

public interface FreezeRecordRepository extends JpaRepository<FreezeRecord, Integer> {
    @Query(value = "SELECT * FROM freeze_record WHERE freeze_date > coalesce(" +
            "(SELECT freeze_date FROM freeze_record WHERE status = 'COMPLETED' ORDER BY freeze_date DESC LIMIT 1), NOW()) " +
            "ORDER BY freeze_date",
            nativeQuery = true)
    List<FreezeRecord> findAllAfterCompleted();

    FreezeRecord findFirstByStatusOrderByFreezeDateDesc(final FreezeRecordStatusEnum freezeRecordStatusEnum);

    FreezeRecord findFirstByStatusOrderByFreezeDateAsc(final FreezeRecordStatusEnum freezeRecordStatusEnum);

    /**
     * Возвращает DTO с ближайшими датами слева и справа к дате последней блокировки
     * @param date дата последней блокировки
     * @return
     */
    @Query(nativeQuery = true)
    UnfreezeDateInterval findBoundaryByFreezeDate(final LocalDate date);

    boolean existsByStatus(final FreezeRecordStatusEnum freezeRecordStatusEnum);
}
