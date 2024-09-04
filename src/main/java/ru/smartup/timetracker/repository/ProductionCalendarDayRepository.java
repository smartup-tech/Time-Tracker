package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.smartup.timetracker.entity.ProductionCalendarDay;

import java.util.List;

@Repository
public interface ProductionCalendarDayRepository extends JpaRepository<ProductionCalendarDay, Long> {
    @Query("SELECT pcd FROM ProductionCalendarDay pcd WHERE EXTRACT(YEAR FROM pcd.day) = :year ORDER BY pcd.day")
    List<ProductionCalendarDay> findAllByYear(final int year);
}
