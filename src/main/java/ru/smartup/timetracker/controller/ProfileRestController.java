package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.core.CurrentSessionEmployeePrincipal;
import ru.smartup.timetracker.core.SessionEmployeePrincipal;
import ru.smartup.timetracker.dto.ErrorCode;
import ru.smartup.timetracker.dto.profile.request.PasswordUpdateDto;
import ru.smartup.timetracker.dto.profile.request.PersonalDataUpdateDto;
import ru.smartup.timetracker.dto.profile.response.ProfileDto;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.exception.ForbiddenException;
import ru.smartup.timetracker.exception.InvalidParameterException;
import ru.smartup.timetracker.service.EmployeeService;

import javax.validation.Valid;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/profile")
public class ProfileRestController {
    private static final String INVALID_CURRENT_PASSWORD = "Current password is not correct";

    private final EmployeeService employeeService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ProfileDto getProfile(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal) {
        Optional<Employee> existEmployee = employeeService.getEmployee(currentSessionEmployeePrincipal.getId());
        if (existEmployee.isEmpty()) {
            throw new ForbiddenException("Employee was not found by employeeId = " + currentSessionEmployeePrincipal.getId() + ".");
        }
        Employee employee = existEmployee.get();
        ProfileDto profileDto = modelMapper.map(employee, ProfileDto.class);
        profileDto.setRoles(currentSessionEmployeePrincipal.getEmployeeRoles());
        profileDto.setProjectRoles(currentSessionEmployeePrincipal.getProjectIdsByProjectRoles().keySet());
        return profileDto;
    }

    @PatchMapping("/updatePersonalData")
    public ProfileDto updatePersonalData(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                                         @Valid @RequestBody PersonalDataUpdateDto personalDataUpdateDto) {
        Optional<Employee> existEmployee = employeeService.getEmployee(currentSessionEmployeePrincipal.getId());
        if (existEmployee.isEmpty()) {
            throw new ForbiddenException("Employee was not found by employeeId = " + currentSessionEmployeePrincipal.getId() + ".");
        }
        Employee employee = existEmployee.get();
        modelMapper.map(personalDataUpdateDto, employee);
        employeeService.updateEmployee(employee);
        ProfileDto profileDto = modelMapper.map(employee, ProfileDto.class);
        profileDto.setRoles(currentSessionEmployeePrincipal.getEmployeeRoles());
        profileDto.setProjectRoles(currentSessionEmployeePrincipal.getProjectIdsByProjectRoles().keySet());
        return profileDto;
    }

    @PatchMapping("/updatePwd")
    public void updatePassword(@CurrentSessionEmployeePrincipal SessionEmployeePrincipal currentSessionEmployeePrincipal,
                               @Valid @RequestBody PasswordUpdateDto passwordUpdateDto) {
        Optional<Employee> existEmployee = employeeService.getEmployee(currentSessionEmployeePrincipal.getId());
        if (existEmployee.isEmpty()) {
            throw new ForbiddenException("Employee was not found by employeeId = " + currentSessionEmployeePrincipal.getId() + ".");
        }
        Employee employee = existEmployee.get();
        if (!passwordEncoder.matches(passwordUpdateDto.getOldPassword(), employee.getPasswordHash())) {
            throw new InvalidParameterException(ErrorCode.NOT_VALID_OLD_PWD, INVALID_CURRENT_PASSWORD);
        }
        employeeService.updatePassword(employee.getId(), employee.getPasswordHash(),
                passwordEncoder.encode(passwordUpdateDto.getNewPassword()));
    }
}
