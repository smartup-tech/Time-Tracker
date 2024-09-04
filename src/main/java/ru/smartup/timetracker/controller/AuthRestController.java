package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {
    @GetMapping("/logout")
    public void logout(HttpServletRequest httpServletRequest) throws ServletException {
        httpServletRequest.logout();
    }
}
