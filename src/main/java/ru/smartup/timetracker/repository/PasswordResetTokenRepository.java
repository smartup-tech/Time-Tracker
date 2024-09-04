package ru.smartup.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.smartup.timetracker.entity.PasswordResetToken;
import ru.smartup.timetracker.entity.field.pk.PasswordResetTokenPK;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, PasswordResetTokenPK> {
    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM PasswordResetToken WHERE userId = :userId")
    void deleteAllByUserId(int userId);
}
