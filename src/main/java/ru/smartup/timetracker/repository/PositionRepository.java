package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.smartup.timetracker.entity.Position;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Integer>, JpaSpecificationExecutor<Position> {
    Optional<Position> findByIdAndIsArchivedFalse(int positionId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM position WHERE name = :positionName)", nativeQuery = true)
    boolean isNotUnique(@Param("positionName") String positionName);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM position WHERE id <> :positionId AND name = :positionName)",
            nativeQuery = true)
    boolean isNotUnique(@Param("positionId") int positionId, @Param("positionName") String positionName);

    @Modifying
    @Query("UPDATE Position SET isArchived = true WHERE id = :positionId")
    void archivePosition(@Param("positionId") int positionId);

    List<Position> findAllByIsArchivedFalseOrderByName();
}
