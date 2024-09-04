package ru.smartup.timetracker.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smartup.timetracker.entity.ProductionCalendarDay;
import ru.smartup.timetracker.repository.ProductionCalendarDayRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductionCalendarService {
    private final ProductionCalendarDayRepository productionCalendarDayRepository;

    @Transactional
    public ProductionCalendarDay addProductionCalendarDay(final ProductionCalendarDay productionCalendarDay) {
        return productionCalendarDayRepository.save(productionCalendarDay);
    }

    public List<ProductionCalendarDay> getAllProductionCalendarDay() {
        return productionCalendarDayRepository.findAllByYear(LocalDate.now().getYear());
    }

    public List<ProductionCalendarDay> getAllProductionCalendarDayByYear(final int year) {
        return productionCalendarDayRepository.findAllByYear(year);
    }

    @Transactional
    public void deleteCalendarDay(final long calendarDayId) {
        productionCalendarDayRepository.deleteById(calendarDayId);
    }
}
