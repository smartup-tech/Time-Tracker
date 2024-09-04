package ru.smartup.timetracker.controller;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.dto.production.calendar.request.ProductionCalendarAddDayDto;
import ru.smartup.timetracker.dto.production.calendar.response.ProductionCalendarDayDto;
import ru.smartup.timetracker.entity.ProductionCalendarDay;
import ru.smartup.timetracker.service.ProductionCalendarService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calendar")
@AllArgsConstructor
public class ProductionCalendarController {
    private final ProductionCalendarService productionCalendarService;
    private final ModelMapper modelMapper;

    @PreAuthorize("getPrincipal().isAdmin()")
    @PostMapping
    public ProductionCalendarDayDto addProductionCalendarDay(@RequestBody @Valid final ProductionCalendarAddDayDto productionCalendarAddDayDto) {
        final ProductionCalendarDay productionCalendarDay = modelMapper.map(productionCalendarAddDayDto, ProductionCalendarDay.class);
        return modelMapper.map(
                productionCalendarService.addProductionCalendarDay(productionCalendarDay),
                ProductionCalendarDayDto.class
        );
    }

    @GetMapping
    @PreAuthorize("getPrincipal().isUser() or getPrincipal().isAdmin() or getPrincipal().isReportReceiver()")
    public List<ProductionCalendarDayDto> getProductionCalendarByYear(@RequestParam(name = "year") Optional<Integer> yearParam) {
        final int year = yearParam.orElseGet(() -> LocalDate.now().getYear());
        return productionCalendarService.getAllProductionCalendarDayByYear(year)
                .stream()
                .map((element) -> modelMapper.map(element, ProductionCalendarDayDto.class))
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{calendarDayId}")
    @PreAuthorize("getPrincipal().isAdmin()")
    public void deleteProductionCalendarDay(@PathVariable("calendarDayId") final long calendarDayId) {
        productionCalendarService.deleteCalendarDay(calendarDayId);
    }
}
