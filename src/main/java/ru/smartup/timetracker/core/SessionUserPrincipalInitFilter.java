package ru.smartup.timetracker.core;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.smartup.timetracker.service.RelationUserRolesService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Фильтр для согласованной установки ролей пользователей в данные новой сессии
 */
@Component
@RequiredArgsConstructor
public class SessionUserPrincipalInitFilter extends OncePerRequestFilter {
    private final RelationUserRolesService relationUserRolesService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            SessionUserPrincipal sessionUserPrincipal = (SessionUserPrincipal) authentication.getPrincipal();
            if (sessionUserPrincipal.getProjectIdsByProjectRoles() == null) {
                relationUserRolesService.setRolesToPrincipalIfNull(sessionUserPrincipal);
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}