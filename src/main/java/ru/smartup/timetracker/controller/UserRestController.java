package ru.smartup.timetracker.controller;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.passay.PasswordGenerator;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import ru.smartup.timetracker.dto.ErrorCode;
import ru.smartup.timetracker.dto.PageableRequestParamDto;
import ru.smartup.timetracker.dto.position.response.PositionDto;
import ru.smartup.timetracker.dto.project.response.ProjectShortDto;
import ru.smartup.timetracker.dto.user.request.UserCreateDto;
import ru.smartup.timetracker.dto.user.request.UserUpdateDto;
import ru.smartup.timetracker.dto.QueryArchiveParamRequestDto;
import ru.smartup.timetracker.dto.user.response.UserDetailDto;
import ru.smartup.timetracker.dto.user.response.UserShortDto;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.entity.field.enumerated.NoticeTypeEnum;
import ru.smartup.timetracker.entity.Position;
import ru.smartup.timetracker.entity.field.sort.UserSortFieldEnum;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.exception.NotProcessedTrackUnitsException;
import ru.smartup.timetracker.exception.NotUniqueDataException;
import ru.smartup.timetracker.exception.RelatedEntitiesFoundException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.pojo.notice.NoticePersonal;
import ru.smartup.timetracker.pojo.notice.NoticePersonalToken;
import ru.smartup.timetracker.service.*;
import ru.smartup.timetracker.service.notification.notifier.NotifierObservable;
import ru.smartup.timetracker.utils.CommonStringUtils;
import ru.smartup.timetracker.validation.validator.PasswordValidator;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserRestController {

    private static final int MAX_NUMBER_OF_USERS = 20;

    private final UserService userService;
    private final PositionService positionService;
    private final RelationUserRolesService relationUserRolesService;
    private final ProjectService projectService;
    private final TrackUnitService trackUnitService;
    private final PasswordResetTokenService passwordResetTokenService;

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotifierObservable notifierObservable;
    private final PasswordGenerator passwordGenerator;
    private final ConversionService conversionService;

    @PreAuthorize("getPrincipal().isAdmin()")
    @GetMapping
    public Page<UserShortDto> getUsersByPage(final @Valid QueryArchiveParamRequestDto userRequestParam,
                                             final @Valid PageableRequestParamDto<UserSortFieldEnum> pageableCriteria) {
        pageableCriteria.setSortBy(conversionService.convert(pageableCriteria.getSortBy(), UserSortFieldEnum.class));
        return userService.getUsers(userRequestParam, pageableCriteria);
    }

    @PreAuthorize("getPrincipal().isAdmin() or getPrincipal().isManager(#projectId)")
    @GetMapping("/project")
    public List<UserShortDto> getUsersForProject(@RequestParam(value = "projectId") int projectId,
                                                 @RequestParam(value = "query", defaultValue = StringUtils.EMPTY) String query) {
        Optional<Project> existProject = projectService.getNotArchivedProject(projectId);
        if (existProject.isEmpty()) {
            throw new ResourceNotFoundException("Active project was not found by projectId = " + projectId + ".");
        }
        Sort sort = Sort.by(Sort.Direction.ASC, UserSortFieldEnum.NAME.getValues());
        Pageable pageable = PageRequest.of(0, MAX_NUMBER_OF_USERS, sort);

        return userService.getUsersForProject(projectId, CommonStringUtils.escapePercentAndUnderscore(query), pageable)
                .stream()
                .map(user -> modelMapper.map(user, UserShortDto.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @GetMapping("/{userId}")
    public UserDetailDto getUser(@PathVariable("userId") int userId) {
        Optional<User> existUser = userService.getUser(userId);
        if (existUser.isEmpty()) {
            throw new ResourceNotFoundException("User was not found by userId = " + userId + ".");
        }
        User user = existUser.get();
        UserDetailDto userDetailDto = modelMapper.map(user, UserDetailDto.class);
        PositionDto positionDto = positionService.getPosition(user.getPositionId())
                .map(position -> modelMapper.map(position, PositionDto.class))
                .orElse(new PositionDto());
        userDetailDto.setPosition(positionDto);
        userDetailDto.setRoles(userService.getUserRoles(userId).stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList()));
        Map<Integer, List<ProjectRoleEnum>> projectRoles = userService.getUserProjectRoles(userId).stream()
                .collect(Collectors.groupingBy(UserProjectRole::getProjectId, Collectors.toList()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(UserProjectRole::getProjectRoleId)
                        .collect(Collectors.toList())));
        userDetailDto.setProjectRoles(projectRoles);
        return userDetailDto;
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @PostMapping
    public UserShortDto createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        Optional<Position> existPosition = positionService.getNotArchivedPosition(userCreateDto.getPositionId());
        if (existPosition.isEmpty()) {
            throw new ResourceNotFoundException("Active position was not found by positionId = "
                    + userCreateDto.getPositionId() + ".");
        }
        if (userService.isNotUnique(userCreateDto.getEmail())) {
            throw new NotUniqueDataException(ErrorCode.NOT_UNIQUE_USER_NAME, "User with specified email = '"
                    + userCreateDto.getEmail() + "' already exists.");
        }
        String password = userCreateDto.getPassword() == null
                ? passwordGenerator.generatePassword(PasswordValidator.MAX_PASSWORD_LENGTH, PasswordValidator.getCharacterRules())
                : userCreateDto.getPassword();
        User user = modelMapper.map(userCreateDto, User.class);
        user.setPasswordHash(passwordEncoder.encode(password));
        int userId = userService.createUser(user);
        List<UserRole> userRoles = userCreateDto.getRoles().stream()
                .map(role -> new UserRole(userId, role))
                .collect(Collectors.toList());
        relationUserRolesService.updateUserRoles(userId, userRoles);
        UserShortDto userShortDto = modelMapper.map(userCreateDto, UserShortDto.class);
        userShortDto.setId(userId);

        String token = passwordResetTokenService.createPasswordResetTokenForRegistration(user.getId());
        long ttlInHours = passwordResetTokenService.getPasswordRegistrationTokenTtlInHours();

        final NoticePersonalToken noticePersonalToken = new NoticePersonalToken(user.getFirstName(), token, ttlInHours);
        final Notice notice = new Notice(NoticeTypeEnum.REGISTER_NEW_USER, noticePersonalToken);

        notifierObservable.notifyEmailChannel(List.of(user), notice);

        return userShortDto;
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @PatchMapping("/{userId}")
    public void updateUser(@Valid @RequestBody UserUpdateDto userUpdateDto, @PathVariable("userId") int userId) {
        Optional<User> existUser = userService.getNotArchivedUser(userId);
        if (existUser.isEmpty()) {
            throw new ResourceNotFoundException("Active user was not found by userId = " + userId + ".");
        }
        Optional<Position> existPosition = positionService.getNotArchivedPosition(userUpdateDto.getPositionId());
        if (existPosition.isEmpty()) {
            throw new ResourceNotFoundException("Active position was not found by positionId = "
                    + userUpdateDto.getPositionId() + ".");
        }
        User user = existUser.get();
        boolean isChangedEmail = false;
        if (!user.getEmail().equals(userUpdateDto.getEmail())) {
            if (userService.isNotUnique(userUpdateDto.getEmail())) {
                throw new NotUniqueDataException(ErrorCode.NOT_UNIQUE_USER_NAME, "User with specified email = '"
                        + userUpdateDto.getEmail() + "' already exists.");
            }
            isChangedEmail = true;
        }
        modelMapper.map(userUpdateDto, user);
        boolean isChangedPassword = false;
        String password = userUpdateDto.getPassword();
        if (password != null) {
            user.setPasswordHash(passwordEncoder.encode(password));
            isChangedPassword = true;
        }
        userService.updateUser(user);
        List<UserRole> userRoles = userUpdateDto.getRoles().stream()
                .map(role -> new UserRole(userId, role))
                .collect(Collectors.toList());
        relationUserRolesService.updateUserRoles(userId, userRoles);
        if (isChangedEmail) {
            userService.invalidateUserSessions(userId);
        }

        if (isChangedPassword) {
            final NoticePersonal noticePersonal = new NoticePersonal(user.getFirstName());
            final Notice notice = new Notice(NoticeTypeEnum.PASSWORD_RESET,noticePersonal);

            notifierObservable.notifyEmailChannel(List.of(user), notice);
        }
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @PostMapping("/{userId}/archive")
    public void archiveUser(@PathVariable("userId") int userId,
                            @RequestParam(value = "force", defaultValue = "false") boolean force) {
        Optional<User> existUser = userService.getNotArchivedUser(userId);
        if (existUser.isEmpty()) {
            throw new ResourceNotFoundException("Active user was not found by userId = " + userId + ".");
        }
        if (trackUnitService.hasNoneFinalTrackUnitForUser(userId)) {
            throw new NotProcessedTrackUnitsException(ErrorCode.NOT_PROCESSED_TRACK_UNITS_FOR_USER,
                    "Archive is not available now. Please, check all not processed track units of user; userId = "
                            + userId + ".");
        }
        if (!force) {
            List<Project> projects = projectService.getNotArchivedProjectsOfUser(userId);
            if (!CollectionUtils.isEmpty(projects)) {
                List<ProjectShortDto> linkedProjects = projects.stream()
                        .map(project -> modelMapper.map(project, ProjectShortDto.class))
                        .collect(Collectors.toList());
                throw new RelatedEntitiesFoundException(ErrorCode.RELATED_ENTITIES_FOUND_FOR_USER,
                        "The specified user will be removed from not archived projects; userId = " + userId + ".",
                        linkedProjects);
            }
        }
        userService.updateArchiveStatus(userId, true);
    }

    @PreAuthorize("getPrincipal().isAdmin()")
    @PostMapping("/{userId}/unArchive")
    public void unArchiveUser(@PathVariable("userId") int userId) {
        userService.unArchiveUser(userId);
    }
}
