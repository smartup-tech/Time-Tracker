package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Optional<Notice> findByIdAndEmployeeId(long id, int employeeId);

    List<Notice> findAllByEmployeeIdOrderByCreatedDateDesc(int employeeId);


    @Query("SELECT n FROM Notice n WHERE n.type = :type AND n.data = :data ORDER BY n.createdDate DESC")
    Optional<Notice>  findNoticeByTypeAndData(@Param("type") NoticeTypeEnum type, @Param("data") Object data);

    @Query("SELECT n FROM Notice n WHERE n.employeeId = :employeeId AND n.deleted = false ORDER BY n.createdDate DESC")
    List<Notice> findAllByEmployeeIdOrderByCreatedDateDescWithoutDeleted(int employeeId);

    int countByEmployeeIdAndReadFalse(int employeeId);

    @Modifying
    @Query("UPDATE Notice SET read = true WHERE employeeId = :employeeId AND read = false")
    void readAllByEmployeeId(int employeeId);

    @Modifying
    @Query("UPDATE Notice SET read = true WHERE id IN :ids AND employeeId = :employeeId AND read = false")
    void readByIdInAndEmployeeId(Set<Long> ids, int employeeId);

    @Modifying
    @Query("DELETE FROM Notice WHERE employeeId = :employeeId")
    void deleteAllByEmployeeId(int employeeId);

    @Modifying
    @Query("UPDATE Notice n SET n.deleted = true WHERE n.id IN :ids AND n.employeeId = :employeeId")
    void setDeletedByIdInAndEmployeeId(@Param("ids") Set<Long> ids, @Param("employeeId") int employeeId);
}
