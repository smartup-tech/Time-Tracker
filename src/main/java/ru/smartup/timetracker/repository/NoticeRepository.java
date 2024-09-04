package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.smartup.timetracker.entity.Notice;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Optional<Notice> findByIdAndUserId(long id, int userId);

    List<Notice> findAllByUserIdOrderByCreatedDateDesc(int userId);

    int countByUserIdAndReadFalse(int userId);

    @Modifying
    @Query("UPDATE Notice SET read = true WHERE userId = :userId AND read = false")
    void readAllByUserId(int userId);

    @Modifying
    @Query("UPDATE Notice SET read = true WHERE id IN :ids AND userId = :userId AND read = false")
    void readByIdInAndUserId(Set<Long> ids, int userId);

    @Modifying
    @Query("DELETE FROM Notice WHERE userId = :userId")
    void deleteAllByUserId(int userId);

    @Modifying
    @Query("DELETE FROM Notice WHERE id IN :ids AND userId = :userId")
    void deleteByIdInAndUserId(Set<Long> ids, int userId);
}
