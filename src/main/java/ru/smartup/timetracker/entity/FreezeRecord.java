package ru.smartup.timetracker.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.FreezeRecordStatusEnum;
import ru.smartup.timetracker.pojo.freeze.UnfreezeDateInterval;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDate;

@NamedNativeQuery(
        name = "FreezeRecord.findBoundaryByFreezeDate",
        query = "SELECT prev_date, next_date FROM " +
                "(SELECT " +
                "freeze_date, " +
                "LAG(freeze_date) OVER (ORDER BY freeze_date) as prev_date, " +
                "LEAD(freeze_date) OVER (ORDER BY freeze_date) as next_date " +
                "FROM freeze_record) as boundary " +
                "WHERE freeze_date = :date",
        resultSetMapping = "Mapping.UnfreezeDateInterval")
@SqlResultSetMapping(
        name = "Mapping.UnfreezeDateInterval",
        classes = @ConstructorResult(
                targetClass = UnfreezeDateInterval.class,
                columns = {
                        @ColumnResult(name = "prev_date", type = LocalDate.class),
                        @ColumnResult(name = "next_date", type = LocalDate.class)}))
@Data
@NoArgsConstructor
@Entity
@Table(name = "freeze_record", schema = "public")
public class FreezeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "freeze_date", unique = true, nullable = false)
    private LocalDate freezeDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", unique = true, nullable = false)
    private Employee employee;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private FreezeRecordStatusEnum status;

    @Column(name = "info")
    private String info;

    @Column(name = "created_date", insertable = false, updatable = false)
    private Timestamp createdDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Timestamp lastModifiedDate;

    public FreezeRecord(final LocalDate freezeDate,
                        final FreezeRecordStatusEnum status,
                        final Employee employee) {
        this.freezeDate = freezeDate;
        this.status = status;
        this.employee = employee;
    }

    public void successful(final int updatedRecords) {
        this.setInfo("Success: updated " + updatedRecords + " rows");
        this.setStatus(FreezeRecordStatusEnum.COMPLETED);
    }

    public void unfreeze(final int updatedRecords) {
        this.setInfo("Unfreeze: updated " + updatedRecords + " rows");
    }

    public void interrupted(final String e) {
        this.setInfo("Error: " + e);
        this.setStatus(FreezeRecordStatusEnum.INTERRUPTED);
    }
}
