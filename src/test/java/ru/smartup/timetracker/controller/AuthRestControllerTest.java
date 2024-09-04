package ru.smartup.timetracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AuthRestControllerTest {
    private final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    private AuthRestController authRestController;

    @BeforeEach
    public void setUp() {
        authRestController = new AuthRestController();
    }

    @Test
    public void logout() throws ServletException {
        authRestController.logout(httpServletRequest);

        verify(httpServletRequest).logout();
    }
}