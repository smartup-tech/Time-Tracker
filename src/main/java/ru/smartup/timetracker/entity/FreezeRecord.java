package ru.smartup.timetracker.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.enumerated.FreezeRecordStatusEnum;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDate;

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
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

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
                        final User user) {
        this.freezeDate = freezeDate;
        this.status = status;
        this.user = user;
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
