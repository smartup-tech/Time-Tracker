package ru.smartup.timetracker.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.core.CurrentSessionUserPrincipal;
import ru.smartup.timetracker.core.SessionUserPrincipal;
import ru.smartup.timetracker.dto.ErrorCode;
import ru.smartup.timetracker.dto.profile.request.PasswordUpdateDto;
import ru.smartup.timetracker.dto.profile.request.PersonalDataUpdateDto;
import ru.smartup.timetracker.dto.profile.response.ProfileDto;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.exception.ForbiddenException;
import ru.smartup.timetracker.exception.InvalidParameterException;
import ru.smartup.timetracker.service.UserService;

import javax.validation.Valid;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/profile")
public class ProfileRestController {
    private static final String INVALID_CURRENT_PASSWORD = "Current password is not correct";

    private final UserService userService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ProfileDto getProfile(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal) {
        Optional<User> existUser = userService.getUser(currentSessionUserPrincipal.getId());
        if (existUser.isEmpty()) {
            throw new ForbiddenException("User was not found by userId = " + currentSessionUserPrincipal.getId() + ".");
        }
        User user = existUser.get();
        ProfileDto profileDto = modelMapper.map(user, ProfileDto.class);
        profileDto.setRoles(currentSessionUserPrincipal.getUserRoles());
        profileDto.setProjectRoles(currentSessionUserPrincipal.getProjectIdsByProjectRoles().keySet());
        return profileDto;
    }

    @PatchMapping("/updatePersonalData")
    public ProfileDto updatePersonalData(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
                                         @Valid @RequestBody PersonalDataUpdateDto personalDataUpdateDto) {
        Optional<User> existUser = userService.getUser(currentSessionUserPrincipal.getId());
        if (existUser.isEmpty()) {
            throw new ForbiddenException("User was not found by userId = " + currentSessionUserPrincipal.getId() + ".");
        }
        User user = existUser.get();
        modelMapper.map(personalDataUpdateDto, user);
        userService.updateUser(user);
        ProfileDto profileDto = modelMapper.map(user, ProfileDto.class);
        profileDto.setRoles(currentSessionUserPrincipal.getUserRoles());
        profileDto.setProjectRoles(currentSessionUserPrincipal.getProjectIdsByProjectRoles().keySet());
        return profileDto;
    }

    @PatchMapping("/updatePwd")
    public void updatePassword(@CurrentSessionUserPrincipal SessionUserPrincipal currentSessionUserPrincipal,
                               @Valid @RequestBody PasswordUpdateDto passwordUpdateDto) {
        Optional<User> existUser = userService.getUser(currentSessionUserPrincipal.getId());
        if (existUser.isEmpty()) {
            throw new ForbiddenException("User was not found by userId = " + currentSessionUserPrincipal.getId() + ".");
        }
        User user = existUser.get();
        if (!passwordEncoder.matches(passwordUpdateDto.getOldPassword(), user.getPasswordHash())) {
            throw new InvalidParameterException(ErrorCode.NOT_VALID_OLD_PWD, INVALID_CURRENT_PASSWORD);
        }
        userService.updatePassword(user.getId(), user.getPasswordHash(),
                passwordEncoder.encode(passwordUpdateDto.getNewPassword()));
    }
}
