package ru.smartup.timetracker.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.smartup.timetracker.core.SessionUserPrincipal;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.profile.request.PasswordUpdateDto;
import ru.smartup.timetracker.dto.profile.request.PersonalDataUpdateDto;
import ru.smartup.timetracker.dto.profile.response.ProfileDto;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.exception.ForbiddenException;
import ru.smartup.timetracker.exception.InvalidParameterException;
import ru.smartup.timetracker.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProfileRestControllerTest {
    private static final int USER_ID = 1;
    private static final int PROJECT_ID = 1;
    private static final String USER_EMAIL = "user_email";
    private static final String USER_FIRST_NAME = "user_first_name";
    private static final String USER_LAST_NAME = "user_last_name";
    private static final String USER_PASSWORD = "admin";
    private static final String USER_PASSWORD_HASH = "$2y$10$3XCy114Ep7LCnTFqKE8B4OyD7XR3mu/ziGVB8XWYKWRx.sxFXmOe2";

    private final UserService userService = mock(UserService.class);
    private ProfileRestController profileRestController;

    @BeforeEach
    public void setUp() {
        WebConfig webConfig = new WebConfig();
        profileRestController = new ProfileRestController(userService, webConfig.modelMapper(), webConfig.passwordEncoder());
    }

    @Test
    public void getProfile() {
        User user = createUser();

        when(userService.getUser(USER_ID)).thenReturn(Optional.of(user));

        ProfileDto profileDto = profileRestController.getProfile(createSessionUserPrincipal());

        assertEquals(user.getEmail(), profileDto.getEmail());
        assertEquals(user.getId(), profileDto.getId());
        assertEquals(1, profileDto.getProjectRoles().size());
        assertTrue(profileDto.getProjectRoles().contains(ProjectRoleEnum.EMPLOYEE));
        assertEquals(1, profileDto.getRoles().size());
        assertTrue(profileDto.getRoles().contains(UserRoleEnum.ROLE_USER));
    }

    @Test
    public void getProfile_shouldReturnException() {
        when(userService.getUser(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> profileRestController.getProfile(createSessionUserPrincipal()));
    }

    @Test
    public void updatePersonalData() {
        User user = createUser();

        when(userService.getUser(USER_ID)).thenReturn(Optional.of(user));

        PersonalDataUpdateDto personalDataUpdateDto = new PersonalDataUpdateDto();
        personalDataUpdateDto.setFirstName(USER_FIRST_NAME);
        personalDataUpdateDto.setLastName(USER_LAST_NAME);

        ProfileDto profileDto = profileRestController.updatePersonalData(createSessionUserPrincipal(), personalDataUpdateDto);

        assertEquals(user.getEmail(), profileDto.getEmail());
        assertEquals(user.getId(), profileDto.getId());
        assertEquals(personalDataUpdateDto.getFirstName(), profileDto.getFirstName());
        assertEquals(personalDataUpdateDto.getLastName(), profileDto.getLastName());
    }

    @Test
    public void updatePersonalData_shouldReturnException() {
        when(userService.getUser(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
                () -> profileRestController.updatePersonalData(createSessionUserPrincipal(), null));
    }

    @Test
    public void updatePassword() {
        PasswordUpdateDto passwordUpdateDto = new PasswordUpdateDto();
        passwordUpdateDto.setOldPassword(USER_PASSWORD);
        passwordUpdateDto.setNewPassword(USER_PASSWORD);

        when(userService.getUser(USER_ID)).thenReturn(Optional.of(createUser()));

        profileRestController.updatePassword(createSessionUserPrincipal(), passwordUpdateDto);

        verify(userService).updatePassword(anyInt(), anyString(), anyString());
    }

    @Test
    public void updatePassword_shouldReturnForbiddenException() {
        when(userService.getUser(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
                () -> profileRestController.updatePassword(createSessionUserPrincipal(), null));
    }

    @Test
    public void updatePassword_shouldReturnInvalidParameterException() {
        PasswordUpdateDto passwordUpdateDto = new PasswordUpdateDto();
        passwordUpdateDto.setOldPassword(StringUtils.EMPTY);

        when(userService.getUser(USER_ID)).thenReturn(Optional.of(createUser()));

        assertThrows(InvalidParameterException.class,
                () -> profileRestController.updatePassword(createSessionUserPrincipal(), passwordUpdateDto));
    }

    private SessionUserPrincipal createSessionUserPrincipal() {
        SessionUserPrincipal sessionUserPrincipal = new SessionUserPrincipal(USER_ID, USER_EMAIL);
        UserRole userRole = new UserRole();
        userRole.setUserId(USER_ID);
        userRole.setRoleId(UserRoleEnum.ROLE_USER);
        UserProjectRole userProjectRole = new UserProjectRole();
        userProjectRole.setUserId(USER_ID);
        userProjectRole.setProjectId(PROJECT_ID);
        userProjectRole.setProjectRoleId(ProjectRoleEnum.EMPLOYEE);
        sessionUserPrincipal.setAllRoles(List.of(userRole), List.of(userProjectRole));
        return sessionUserPrincipal;
    }

    private User createUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        user.setPasswordHash(USER_PASSWORD_HASH);
        return user;
    }
}