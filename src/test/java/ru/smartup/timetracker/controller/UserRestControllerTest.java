package ru.smartup.timetracker.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.*;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.PageableRequestParamDto;
import ru.smartup.timetracker.dto.user.request.UserCreateDto;
import ru.smartup.timetracker.dto.user.request.UserUpdateDto;
import ru.smartup.timetracker.dto.QueryArchiveParamRequestDto;
import ru.smartup.timetracker.dto.user.response.UserDetailDto;
import ru.smartup.timetracker.dto.user.response.UserShortDto;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.Position;
import ru.smartup.timetracker.entity.field.sort.UserSortFieldEnum;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.exception.NotProcessedTrackUnitsException;
import ru.smartup.timetracker.exception.NotUniqueDataException;
import ru.smartup.timetracker.exception.RelatedEntitiesFoundException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.service.*;
import ru.smartup.timetracker.service.notification.notifier.NotifierObservable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UserRestControllerTest {
    private static final int PAGE = 1;
    private static final int SIZE = 10;
    private static final int MAX_NUMBER_OF_USERS = 20;
    private static final int PROJECT_ID = 1;
    private static final int POSITION_ID = 1;
    private static final int POSITION_ID_NEW = 2;
    private static final int USER_ID = 1;
    private static final String USER_EMAIL = "user_email";
    private static final String USER_EMAIL_NEW = "user_email_new";
    private static final String USER_FIRST_NAME = "user_first_name";
    private static final String USER_PASSWORD_NEW = "user_pwd_new";
    private static final String NAME_PROPERTY = "name";
    private static final String PASSWORD_REGISTRATION_LINK = "http://localhost:5173/set-password?token=";
    private static final String USER_REGISTRATION_SUBJECT = "Добро пожаловать";
    private static final String USER_REGISTRATION_TEMPLATE = "userRegistration.html";
    private static final String PASSWORD_RESET_SUBJECT = "Сброс пароля";
    private static final String PASSWORD_RESET_TEMPLATE = "passwordReset.html";

    private final UserService userService = mock(UserService.class);
    private final PositionService positionService = mock(PositionService.class);
    private final RelationUserRolesService relationUserRolesService = mock(RelationUserRolesService.class);
    private final ProjectService projectService = mock(ProjectService.class);
    private final TrackUnitService trackUnitService = mock(TrackUnitService.class);
    private final PasswordResetTokenService passwordResetTokenService = mock(PasswordResetTokenService.class);
    private final NotifierObservable notifierObservable = mock(NotifierObservable.class);
    private final ConversionService conversionService = mock(ConversionService.class);

    private UserRestController userRestController;

    private ModelMapper modelMapper;

    @BeforeEach
    public void setUp() {
        WebConfig webConfig = new WebConfig();
        modelMapper = webConfig.modelMapper();
        userRestController = new UserRestController(userService, positionService, relationUserRolesService,
                projectService, trackUnitService, passwordResetTokenService, modelMapper, webConfig.passwordEncoder(),
                notifierObservable, webConfig.passwordGenerator(), conversionService);
    }

    @Test
    public void getUsersByPage() {
        Page<User> users = new PageImpl<>(List.of(createUserObj()));
        Page<UserShortDto> userDtos = users.map(user -> modelMapper.map(user, UserShortDto.class));

        QueryArchiveParamRequestDto userParam = createUserParam("", false);
        PageableRequestParamDto pageableParam = createPageableParam(PAGE, SIZE, UserSortFieldEnum.NAME, Sort.Direction.ASC);

        when(userService.getUsers(userParam, pageableParam)).thenReturn(userDtos);

        when(userService.getUsersRoles(List.of(USER_ID))).thenReturn(Map.of(USER_ID, List.of(createUserRole())));

        Page<UserShortDto> usersByPage = userRestController.getUsersByPage(userParam, pageableParam);

        verify(userService).getUsers(userParam, pageableParam);

        assertEquals(1, usersByPage.getTotalElements());
    }

    @Test
    public void getUsersByPage_whenSearchQuery() {
        Page<User> users = new PageImpl<>(List.of(createUserObj()));
        Page<UserShortDto> userDtos = users.map(user -> modelMapper.map(user, UserShortDto.class));

        QueryArchiveParamRequestDto userParam = createUserParam(USER_FIRST_NAME, false);
        PageableRequestParamDto pageableParam = createPageableParam(PAGE, SIZE, UserSortFieldEnum.NAME, Sort.Direction.ASC);

        when(userService.getUsers(userParam, pageableParam)).thenReturn(userDtos);
        when(userService.getUsersRoles(List.of(USER_ID))).thenReturn(Map.of(USER_ID, List.of(createUserRole())));

        Page<UserShortDto> usersByPage = userRestController.getUsersByPage(userParam, pageableParam);

        verify(userService).getUsers(userParam, pageableParam);
        assertEquals(1, usersByPage.getTotalElements());
    }

    @Test
    public void getUsersForProject() {
        Pageable pageable = PageRequest.of(0, MAX_NUMBER_OF_USERS,
                Sort.by(Sort.Direction.ASC, UserSortFieldEnum.NAME.getValues()));

        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.of(new Project()));
        when(userService.getUsersForProject(PROJECT_ID, StringUtils.EMPTY, pageable)).thenReturn(List.of(createUserObj()));

        List<UserShortDto> usersForProject = userRestController.getUsersForProject(PROJECT_ID, StringUtils.EMPTY);

        assertEquals(1, usersForProject.size());
        assertEquals(USER_ID, usersForProject.get(0).getId());
        assertEquals(USER_EMAIL, usersForProject.get(0).getEmail());
        assertEquals(USER_FIRST_NAME, usersForProject.get(0).getFirstName());
    }

    @Test
    public void getUsersForProject_shouldReturnResourceNotFoundException() {
        when(projectService.getNotArchivedProject(PROJECT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userRestController.getUsersForProject(PROJECT_ID, StringUtils.EMPTY));
    }

    @Test
    public void getUser() {
        when(userService.getUser(USER_ID)).thenReturn(Optional.of(createUserObj()));
        when(positionService.getPosition(POSITION_ID)).thenReturn(Optional.of(createPosition(POSITION_ID)));
        when(userService.getUserRoles(USER_ID)).thenReturn(List.of(createUserRole()));
        when(userService.getUserProjectRoles(USER_ID)).thenReturn(List.of(createUserProjectRole()));

        UserDetailDto userDetailDto = userRestController.getUser(USER_ID);

        assertEquals(1, userDetailDto.getRoles().size());
        assertEquals(UserRoleEnum.ROLE_USER, userDetailDto.getRoles().get(0));
        assertEquals(1, userDetailDto.getProjectRoles().size());
        assertEquals(ProjectRoleEnum.MANAGER, userDetailDto.getProjectRoles().get(PROJECT_ID).get(0));
        assertEquals(USER_ID, userDetailDto.getId());
        assertEquals(USER_EMAIL, userDetailDto.getEmail());
        assertEquals(USER_FIRST_NAME, userDetailDto.getFirstName());
    }

    @Test
    public void getUser_shouldReturnResourceNotFoundException() {
        when(userService.getUser(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userRestController.getUser(USER_ID));
    }

    @Test
    public void createUser() {
        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.of(createPosition(POSITION_ID)));
        when(userService.isNotUnique(USER_EMAIL)).thenReturn(false);

        UserCreateDto userCreateDto = createUserCreateDto();

        userRestController.createUser(userCreateDto);

        verify(userService).createUser(argThat(user -> user.getEmail().equals(USER_EMAIL)
                && (user.getPositionId() == POSITION_ID)));
        verify(relationUserRolesService).updateUserRoles(anyInt(), any());
        verify(passwordResetTokenService).createPasswordResetTokenForRegistration(anyInt());

        verify(notifierObservable).notifyEmailChannel(anyList(), any(Notice.class));
    }

    @Test
    public void createUser_shouldReturnResourceNotFoundException() {
        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userRestController.createUser(createUserCreateDto()));
    }

    @Test
    public void createUser_shouldReturnNotUniqueDataException() {
        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.of(createPosition(POSITION_ID)));
        when(userService.isNotUnique(USER_EMAIL)).thenReturn(true);

        assertThrows(NotUniqueDataException.class, () -> userRestController.createUser(createUserCreateDto()));
    }

    @Test
    public void updateUser() {
        when(userService.getNotArchivedUser(USER_ID)).thenReturn(Optional.of(createUserObj()));
        when(positionService.getNotArchivedPosition(POSITION_ID_NEW)).thenReturn(Optional.of(createPosition(POSITION_ID_NEW)));

        userRestController.updateUser(createUserUpdateDto(USER_EMAIL), USER_ID);

        verify(userService).updateUser(argThat(user -> user.getEmail().equals(USER_EMAIL)
                && (user.getPositionId() == POSITION_ID_NEW)));
        verify(relationUserRolesService).updateUserRoles(USER_ID, List.of(createUserRole()));
        verify(userService, never()).invalidateUserSessions(USER_ID);
    }

    @Test
    public void updateUser_shouldReturnResourceNotFoundExceptionForUser() {
        when(userService.getNotArchivedUser(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userRestController.updateUser(
                createUserUpdateDto(USER_EMAIL), USER_ID));
    }

    @Test
    public void updateUser_shouldReturnResourceNotFoundExceptionForPosition() {
        when(userService.getNotArchivedUser(USER_ID)).thenReturn(Optional.of(createUserObj()));
        when(positionService.getNotArchivedPosition(POSITION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userRestController.updateUser(
                createUserUpdateDto(USER_EMAIL), USER_ID));
    }

    @Test
    public void updateUser_shouldReturnNotUniqueDataException() {
        when(userService.getNotArchivedUser(USER_ID)).thenReturn(Optional.of(createUserObj()));
        when(positionService.getNotArchivedPosition(POSITION_ID_NEW)).thenReturn(Optional.of(createPosition(POSITION_ID_NEW)));
        when(userService.isNotUnique(USER_EMAIL_NEW)).thenReturn(true);

        assertThrows(NotUniqueDataException.class, () -> userRestController.updateUser(
                createUserUpdateDto(USER_EMAIL_NEW), USER_ID));
    }

    @Test
    public void updateUser_whenUpdateEmail() {
        when(userService.getNotArchivedUser(USER_ID)).thenReturn(Optional.of(createUserObj()));
        when(positionService.getNotArchivedPosition(POSITION_ID_NEW)).thenReturn(Optional.of(createPosition(POSITION_ID_NEW)));
        when(userService.isNotUnique(USER_EMAIL_NEW)).thenReturn(false);

        userRestController.updateUser(createUserUpdateDto(USER_EMAIL_NEW), USER_ID);

        verify(userService).updateUser(argThat(user -> user.getEmail().equals(USER_EMAIL_NEW)
                && (user.getPositionId() == POSITION_ID_NEW)));
        verify(relationUserRolesService).updateUserRoles(USER_ID, List.of(createUserRole()));
        verify(userService).invalidateUserSessions(USER_ID);
    }

    @Test
    public void updateUser_whenUpdatePassword() {
        when(userService.getNotArchivedUser(USER_ID)).thenReturn(Optional.of(createUserObj()));
        when(positionService.getNotArchivedPosition(POSITION_ID_NEW)).thenReturn(Optional.of(createPosition(POSITION_ID_NEW)));
        UserUpdateDto userUpdateDto = createUserUpdateDto(USER_EMAIL);
        userUpdateDto.setPassword(USER_PASSWORD_NEW);

        userRestController.updateUser(userUpdateDto, USER_ID);

        verify(userService).updateUser(argThat(user -> user.getEmail().equals(USER_EMAIL)
                && (user.getPositionId() == POSITION_ID_NEW)));
        verify(relationUserRolesService).updateUserRoles(USER_ID, List.of(createUserRole()));
        verify(userService, never()).invalidateUserSessions(USER_ID);

        verify(notifierObservable).notifyEmailChannel(anyList(), any(Notice.class));
    }

    @Test
    public void archiveUser() {
        when(userService.getNotArchivedUser(USER_ID)).thenReturn(Optional.of(createUserObj()));
        when(trackUnitService.hasNoneFinalTrackUnitForUser(USER_ID)).thenReturn(false);
        when(projectService.getNotArchivedProjectsOfUser(USER_ID)).thenReturn(List.of());

        userRestController.archiveUser(USER_ID, false);

        verify(userService).updateArchiveStatus(USER_ID, true);
    }

    @Test
    public void archiveUser_shouldReturnRelatedEntitiesFoundException() {
        when(userService.getNotArchivedUser(USER_ID)).thenReturn(Optional.of(createUserObj()));
        when(trackUnitService.hasNoneFinalTrackUnitForUser(USER_ID)).thenReturn(false);
        when(projectService.getNotArchivedProjectsOfUser(USER_ID)).thenReturn(List.of(new Project()));

        assertThrows(RelatedEntitiesFoundException.class, () -> userRestController.archiveUser(USER_ID, false));
    }

    @Test
    public void archiveUser_whenForceTrue() {
        when(userService.getNotArchivedUser(USER_ID)).thenReturn(Optional.of(createUserObj()));
        when(trackUnitService.hasNoneFinalTrackUnitForUser(USER_ID)).thenReturn(false);

        userRestController.archiveUser(USER_ID, true);

        verify(userService).updateArchiveStatus(USER_ID, true);
        verify(projectService, never()).getNotArchivedProjectsOfUser(USER_ID);
    }

    @Test
    public void archiveUser_shouldReturnResourceNotFoundException() {
        when(userService.getNotArchivedUser(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userRestController.archiveUser(USER_ID, false));
    }

    @Test
    public void archiveUser_shouldReturnNotProcessedTrackUnitsException() {
        when(userService.getNotArchivedUser(USER_ID)).thenReturn(Optional.of(createUserObj()));
        when(trackUnitService.hasNoneFinalTrackUnitForUser(USER_ID)).thenReturn(true);

        assertThrows(NotProcessedTrackUnitsException.class, () -> userRestController.archiveUser(USER_ID, false));
    }

    private User createUserObj() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        user.setFirstName(USER_FIRST_NAME);
        user.setPositionId(POSITION_ID);
        return user;
    }

    private UserRole createUserRole() {
        UserRole userRole = new UserRole();
        userRole.setUserId(USER_ID);
        userRole.setRoleId(UserRoleEnum.ROLE_USER);
        return userRole;
    }

    private UserProjectRole createUserProjectRole() {
        UserProjectRole userProjectRole = new UserProjectRole();
        userProjectRole.setUserId(USER_ID);
        userProjectRole.setProjectId(PROJECT_ID);
        userProjectRole.setProjectRoleId(ProjectRoleEnum.MANAGER);
        return userProjectRole;
    }

    private Position createPosition(int positionId) {
        Position position = new Position();
        position.setId(positionId);
        return position;
    }

    private UserCreateDto createUserCreateDto() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setFirstName(USER_FIRST_NAME);
        userCreateDto.setPositionId(POSITION_ID);
        userCreateDto.setEmail(USER_EMAIL);
        userCreateDto.setPassword(StringUtils.EMPTY);
        userCreateDto.setRoles(List.of(UserRoleEnum.ROLE_USER));
        return userCreateDto;
    }

    private UserUpdateDto createUserUpdateDto(String email) {
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setFirstName(USER_FIRST_NAME);
        userUpdateDto.setPositionId(POSITION_ID_NEW);
        userUpdateDto.setEmail(email);
        userUpdateDto.setRoles(List.of(UserRoleEnum.ROLE_USER));
        return userUpdateDto;
    }

    private QueryArchiveParamRequestDto createUserParam(String query, boolean archive) {
        QueryArchiveParamRequestDto paramRequest = new QueryArchiveParamRequestDto();
        paramRequest.setQuery(query);
        paramRequest.setArchive(archive);
        return paramRequest;
    }

    private PageableRequestParamDto createPageableParam(int page, int size, UserSortFieldEnum sortBy, Sort.Direction sortDirection) {
        PageableRequestParamDto pageableRequestParamDto = new PageableRequestParamDto();
        pageableRequestParamDto.setPage(page);
        pageableRequestParamDto.setSize(size);
        pageableRequestParamDto.setSortBy(sortBy);
        pageableRequestParamDto.setSortDirection(sortDirection);
        return pageableRequestParamDto;
    }
}