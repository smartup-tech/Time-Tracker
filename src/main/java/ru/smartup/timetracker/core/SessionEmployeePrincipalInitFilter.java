package ru.smartup.timetracker.core;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.smartup.timetracker.service.RelationEmployeeRolesService;

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
public class SessionEmployeePrincipalInitFilter extends OncePerRequestFilter {
    private final RelationEmployeeRolesService relationEmployeeRolesService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            SessionEmployeePrincipal sessionEmployeePrincipal = (SessionEmployeePrincipal) authentication.getPrincipal();
            if (sessionEmployeePrincipal.getProjectIdsByProjectRoles() == null) {
                relationEmployeeRolesService.setRolesToPrincipalIfNull(sessionEmployeePrincipal);
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}