package ru.smartup.timetracker.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.smartup.timetracker.entity.field.pk.PasswordResetTokenPK;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "password_reset_token", schema = "public")
@IdClass(PasswordResetTokenPK.class)
public class PasswordResetToken {
    @Id
    @Column(name = "user_id")
    private int userId;
    @Id
    @Column(name = "token")
    private String token;
    @Column(name = "token_expiry", updatable = false)
    private Timestamp tokenExpiry;
}
