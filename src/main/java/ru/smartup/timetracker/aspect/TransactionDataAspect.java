package ru.smartup.timetracker.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.smartup.timetracker.core.SessionUserPrincipal;

@RequiredArgsConstructor
@Aspect
@Component
public class TransactionDataAspect {
    private static final String SESSION_DATA_QUERY = "SELECT set_config('session.user_id', '%s', true)";

    private final JdbcTemplate jdbcTemplate;

    @Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void callTransactionalMethod() {
    }

    @Before("callTransactionalMethod()")
    public void setSessionDataToTransaction() {
        int userId = 0;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            userId = ((SessionUserPrincipal) authentication.getPrincipal()).getId();
        }
        jdbcTemplate.execute(String.format(SESSION_DATA_QUERY, userId));
    }
}
