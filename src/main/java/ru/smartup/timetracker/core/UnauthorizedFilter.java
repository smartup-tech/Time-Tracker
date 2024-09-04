package ru.smartup.timetracker.core;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Фильтр для возврата ошибки аутентификации для указанных url
 */
@Component
public class UnauthorizedFilter extends OncePerRequestFilter {
    private static final String API_PATH = "/api/";
    private static final String API_AUTH_PATH = "/api/auth/";
    private static final String API_PASSWORD_RECOVERY_PATH = "/api/passwordRecovery";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (isAuthenticated() || request.getServletPath().startsWith(API_AUTH_PATH)
                || request.getServletPath().startsWith(API_PASSWORD_RECOVERY_PATH)) {
            filterChain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getServletPath().startsWith(API_PATH);
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication == null)
                || AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            return false;
        }
        return authentication.isAuthenticated();
    }
}