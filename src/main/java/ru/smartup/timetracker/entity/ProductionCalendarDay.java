package ru.smartup.timetracker.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.smartup.timetracker.entity.field.enumerated.ProductionCalendarDayEnum;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "production_calendar_day")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductionCalendarDay {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "production_calendar_day_id")
    @SequenceGenerator(name = "production_calendar_day_id", sequenceName = "production_calendar_day_id_seq", allocationSize = 1)
    private long id;

    @Column(name = "hours", nullable = false)
    private float hours;

    @Column(name = "day", nullable = false, unique = true)
    private Date day;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductionCalendarDayEnum status;

    @Column(name = "created_date", insertable = false, updatable = false)
    private Timestamp createdDate;
}
