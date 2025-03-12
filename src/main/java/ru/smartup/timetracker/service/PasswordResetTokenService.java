package ru.smartup.timetracker.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smartup.timetracker.dto.password.recovery.request.PasswordResetDto;
import ru.smartup.timetracker.entity.PasswordResetToken;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.exception.InvalidTokenException;
import ru.smartup.timetracker.repository.PasswordResetTokenRepository;
import ru.smartup.timetracker.repository.EmployeeRepository;
import ru.smartup.timetracker.utils.CommonStringUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetTokenService {
    private static final long SECONDS_IN_HOUR = 3600;

    private final long passwordRegistrationTokenTtl;
    private final long passwordRegistrationTokenTtlInHours;
    private final long passwordRecoveryTokenTtl;
    private final long passwordRecoveryTokenTtlInHours;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmployeeRepository employeeRepository;

    public PasswordResetTokenService(@Value("${token.registration.ttl}") long passwordRegistrationTokenTtl,
                                     @Value("${token.recovery.ttl}") long passwordRecoveryTokenTtl,
                                     final PasswordEncoder passwordEncoder, PasswordResetTokenRepository passwordResetTokenRepository,
                                     EmployeeRepository employeeRepository) {
        this.passwordRegistrationTokenTtl = passwordRegistrationTokenTtl;
        this.passwordRecoveryTokenTtl = passwordRecoveryTokenTtl;
        this.passwordRegistrationTokenTtlInHours = passwordRegistrationTokenTtl / SECONDS_IN_HOUR;
        this.passwordRecoveryTokenTtlInHours = passwordRecoveryTokenTtl / SECONDS_IN_HOUR;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.employeeRepository = employeeRepository;
    }

    public Optional<PasswordResetToken> getPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Transactional
    public String createPasswordResetTokenForRegistration(int employeeId) {
        return createPasswordResetToken(employeeId, passwordRegistrationTokenTtl);
    }

    @Transactional
    public String createPasswordResetTokenForRecovery(int employeeId) {
        return createPasswordResetToken(employeeId, passwordRecoveryTokenTtl);
    }

    private String createPasswordResetToken(int employeeId, long ttl) {
        String token = UUID.randomUUID().toString().replaceAll(CommonStringUtils.DASH, StringUtils.EMPTY);
        Instant tokenExpiry = Instant.now().plus(ttl, ChronoUnit.SECONDS);
        PasswordResetToken passwordResetToken = new PasswordResetToken(employeeId,
                CommonStringUtils.hashSHA256(token), Timestamp.from(tokenExpiry));
        passwordResetTokenRepository.save(passwordResetToken);
        return token;
    }

    @Transactional
    public void deletePasswordResetToken(PasswordResetToken passwordResetToken) {
        passwordResetTokenRepository.delete(passwordResetToken);
    }

    public Optional<Employee> resetPassword(final PasswordResetDto passwordResetDto) {
        Optional<PasswordResetToken> existPasswordResetToken =
                this.getPasswordResetToken(CommonStringUtils.hashSHA256(passwordResetDto.getToken()));

        if (existPasswordResetToken.isEmpty()) {
            throw new InvalidTokenException("Invalid password reset token.");
        }

        PasswordResetToken passwordResetToken = existPasswordResetToken.get();

        if (passwordResetToken.getTokenExpiry().before(Timestamp.from(Instant.now()))) {
            this.deletePasswordResetToken(passwordResetToken);
            throw new InvalidTokenException("Password reset token has expired.");
        }

        return updatePassword(passwordResetToken.getEmployeeId(), passwordResetDto.getNewPassword());
    }

    @Transactional
    public Optional<Employee> updatePassword(int employeeId, String password) {
        final String passwordHash = passwordEncoder.encode(password);

        passwordResetTokenRepository.deleteAllByEmployeeId(employeeId);

        return employeeRepository.updatePassword(employeeId, passwordHash);
    }

    public long getPasswordRecoveryTokenTtlInHours() {
        return passwordRecoveryTokenTtlInHours;
    }

    public long getPasswordRegistrationTokenTtlInHours() {
        return passwordRegistrationTokenTtlInHours;
    }
}
