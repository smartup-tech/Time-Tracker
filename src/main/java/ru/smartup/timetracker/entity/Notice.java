package ru.smartup.timetracker.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;

import javax.persistence.*;
import java.sql.Timestamp;

@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
@NoArgsConstructor
@Entity
@Table(name = "notice", schema = "public")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private NoticeTypeEnum type;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "text")
    private String text;

    @Type(type = "jsonb")
    @Column(name = "data")
    private Object data;

    @Column(name = "read")
    private boolean read;

    @Column(name = "created_by", updatable = false)
    private int createdBy;

    @Column(name = "created_date", insertable = false, updatable = false)
    private Timestamp createdDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Timestamp lastModifiedDate;

    public Notice(NoticeTypeEnum type, int userId, String text, Object data, int createdBy) {
        this.type = type;
        this.userId = userId;
        this.text = text;
        this.data = data;
        this.createdBy = createdBy;
    }

    public Notice(NoticeTypeEnum type, Object data) {
        this.type = type;
        this.data = data;
    }
}
