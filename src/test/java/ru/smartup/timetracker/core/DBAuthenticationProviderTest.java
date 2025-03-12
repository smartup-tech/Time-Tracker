package ru.smartup.timetracker.core;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.service.EmployeeService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DBAuthenticationProviderTest {
    private static final int EMPLOYEE_ID = 1;
    private static final String EMAIL = "admin@smartup.ru";
    private static final String PASSWORD = "admin";
    private static final String PASSWORD_HASH = "$2y$10$3XCy114Ep7LCnTFqKE8B4OyD7XR3mu/ziGVB8XWYKWRx.sxFXmOe2";

    private final EmployeeService employeeService = mock(EmployeeService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final DBAuthenticationProvider dbAuthenticationProvider =
            new DBAuthenticationProvider(employeeService, passwordEncoder);

    @Test
    public void authenticateByAuthenticationShouldThrowException() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD);
        when(employeeService.getNotArchivedEmployeeByEmail(EMAIL)).thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () ->
                dbAuthenticationProvider.authenticate(authentication));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    public void authenticateByAuthenticationShouldThrowException_1() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD);
        when(employeeService.getNotArchivedEmployeeByEmail(EMAIL)).thenReturn(createEmployee());
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () ->
                dbAuthenticationProvider.authenticate(authentication));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    public void authenticateByAuthenticationShouldReturnToken() {
        SessionEmployeePrincipal principal = new SessionEmployeePrincipal(EMPLOYEE_ID, EMAIL);
        Authentication authentication = new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD);
        when(employeeService.getNotArchivedEmployeeByEmail(EMAIL)).thenReturn(createEmployee());
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);

        Authentication result = dbAuthenticationProvider.authenticate(authentication);

        assertEquals(principal, result.getPrincipal());
        assertNull(result.getCredentials());
        assertTrue(CollectionUtils.isEmpty(result.getAuthorities()));
    }

    private Optional<Employee> createEmployee() {
        Employee employee = new Employee();
        employee.setId(EMPLOYEE_ID);
        employee.setEmail(EMAIL);
        employee.setPasswordHash(PASSWORD_HASH);
        employee.setArchived(false);
        return Optional.of(employee);
    }
}