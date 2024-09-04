package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.smartup.timetracker.entity.FreezeRecord;
import ru.smartup.timetracker.entity.field.enumerated.FreezeRecordStatusEnum;

import java.sql.Date;
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

    @Query(value = "SELECT prev_day, next_day FROM " +
            "(SELECT " +
                "freeze_date, " +
                "LAG(freeze_date) OVER (ORDER BY freeze_date) as prev_day, " +
                "LEAD(freeze_date) OVER (ORDER BY freeze_date) as next_day " +
            "FROM freeze_record) as boundary " +
            "WHERE freeze_date = :date", nativeQuery = true)
    List<Date> findBoundaryByFreezeDate(final LocalDate date);

    boolean existsByStatus(final FreezeRecordStatusEnum freezeRecordStatusEnum);
}
