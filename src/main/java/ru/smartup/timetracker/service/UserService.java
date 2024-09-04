package ru.smartup.timetracker.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smartup.timetracker.core.SessionUserPrincipal;
import ru.smartup.timetracker.dto.PageableRequestParamDto;
import ru.smartup.timetracker.dto.QueryArchiveParamRequestDto;
import ru.smartup.timetracker.dto.user.response.UserShortDto;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.entity.field.sort.UserSortFieldEnum;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.repository.UserProjectRoleRepository;
import ru.smartup.timetracker.repository.UserRepository;
import ru.smartup.timetracker.repository.UserRoleRepository;
import ru.smartup.timetracker.repository.criteria.UserFilterBuilder;
import ru.smartup.timetracker.utils.PageableMaker;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserProjectRoleRepository userProjectRoleRepository;
    private final SessionRegistry sessionRegistry;

    private final PageableMaker pageableMaker;
    private final ModelMapper modelMapper;

    public Page<UserShortDto> getUsers(final QueryArchiveParamRequestDto userParams, final PageableRequestParamDto<UserSortFieldEnum> pageableParams) {

        Page<User> users = getPageableAndFilteredUsers(userParams, pageableParams);

        Map<Integer, List<UserRole>> userRolesMap = getUsersRoles(users.getContent());

        return users
                .map(user -> modelMapper.map(user, UserShortDto.class))
                .map(userShortDto -> setUserRoleInDto(userShortDto, userRolesMap));
    }

    private Page<User> getPageableAndFilteredUsers(final QueryArchiveParamRequestDto userParams, final PageableRequestParamDto<UserSortFieldEnum> pageableParams) {
        Pageable pageable = pageableMaker.make(pageableParams);
        Specification<User> useSpec = getUserFilters(userParams.getQuery(), userParams.isArchive());
        return getPageableAndFilteredUsers(useSpec, pageable);
    }

    public Page<User> getPageableAndFilteredUsers(final Specification<User> useSpec, final Pageable pageable) {
        return userRepository.findAll(useSpec, pageable);
    }

    private Specification<User> getUserFilters(final String searchValue,
                                               final boolean archive) {
        UserFilterBuilder builder = new UserFilterBuilder();

        builder.addIsArchiveFilter(archive);

        if (!searchValue.isBlank()) {
            builder.addNameFilter(searchValue);
        }

        return builder.buildSpecification();
    }

    private UserShortDto setUserRoleInDto(final UserShortDto dto, final Map<Integer, List<UserRole>> userIdToRoles) {
        List<UserRoleEnum> roles = userIdToRoles.get(dto.getId())
                .stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        dto.setRoles(roles);
        return dto;
    }

    public List<User> getUsersForProject(int projectId, String searchValue, Pageable pageable) {
        return userRepository.findCandidatesForProject(projectId, searchValue, pageable);
    }

    public Optional<User> getUser(int userId) {
        return userRepository.findById(userId);
    }

    public List<User> getUsers(Collection<Integer> userIds) {
        return userRepository.findAllById(userIds);
    }

    public Optional<User> getNotArchivedUser(int userId) {
        return userRepository.findByIdAndIsArchivedFalse(userId);
    }

    public List<User> getNotArchivedUsers() {
       return userRepository.findAllByIsArchivedFalse();
    }

    public Optional<User> getArchivedUser(final int userId) {
        return userRepository
                .findByIdAndIsArchivedTrue(userId);
    }

    public Optional<User> getNotArchivedUserByEmail(String email) {
        return userRepository.findByEmailAndIsArchivedFalse(email);
    }


    public List<User> getUsersByRoles(final List<UserRoleEnum> userRoles) {
        return userRepository.findAllByUserRoles(userRoles);
    }

    public List<User> getUsersByRole(final UserRoleEnum userRoleEnum) {
        return userRepository.findAllByUserRole(userRoleEnum);
    }

    public List<User> getUsersByProjectAndProjectRole(final int projectId, final ProjectRoleEnum userProjectRole) {
        return userRepository.findAllByProjectIdAndProjectRole(projectId, userProjectRole);
    }

    public List<UserRole> getUserRoles(int userId) {
        return userRoleRepository.findAllByUserId(userId);
    }

    public List<UserRole> getUserRoles(UserRoleEnum roleId) {
        return userRoleRepository.findAllByRoleId(roleId);
    }

    public List<UserRole> getUserRoles(Set<UserRoleEnum> roleIds) {
        return userRoleRepository.findAllByRoleIdIn(roleIds);
    }

    public Map<Integer, List<UserRole>> getUsersRoles(final Collection<User> users) {
        List<Integer> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());
        return getUsersRoles(userIds);
    }

    public Map<Integer, List<UserRole>> getUsersRoles(List<Integer> userIds) {
        return userRoleRepository.findAllByUserIdIn(userIds).stream()
                .collect(Collectors.groupingBy(UserRole::getUserId, Collectors.toList()));
    }

    public List<UserProjectRole> getUserProjectRoles(int userId) {
        return userProjectRoleRepository.findAllByUserId(userId);
    }

    public List<User> getNotArchivedUsersWithPosition(int positionId) {
        return userRepository.findByPositionIdAndIsArchivedFalse(positionId);
    }

    /**
     * Получить данные пользователей проекта
     *
     * @param projectId идентификатор проекта
     * @return List<UserInProject>
     */
    public List<User> getUsersFromProject(int projectId) {
        return userRepository.findAllUsersInProject(projectId);
    }

    public boolean isNotUnique(String email) {
        return userRepository.isNotUnique(email);
    }

    @Transactional
    public int createUser(User user) {
        return userRepository.save(user).getId();
    }

    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void updateArchiveStatus(int userId, boolean archived) {
        if (archived) {
            userProjectRoleRepository.deleteFromNotArchivedProjectsByUserId(userId);
            userRepository.updateArchiveStatus(userId, archived);
            invalidateUserSessions(userId);
        } else {
            userRepository.updateArchiveStatus(userId, archived);
        }
    }

    public void invalidateUserSessions(int userId) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            SessionUserPrincipal sessionUserPrincipal = (SessionUserPrincipal) principal;
            if (sessionUserPrincipal.getId() == userId) {
                sessionRegistry.getAllSessions(sessionUserPrincipal, false)
                        .forEach(SessionInformation::expireNow);
                break;
            }
        }
    }

    @Transactional
    public void updatePassword(int userId, String oldPasswordHash, String newPasswordHash) {
        userRepository.updatePassword(userId, oldPasswordHash, newPasswordHash);
    }

    public List<User> searchUsers(final String searchValue, final boolean archive, final Sort sort) {
        Specification<User> spec = getUserFilters(searchValue, archive);
        return userRepository.findAll(spec, sort);
    }

    public List<User> searchUsersFromProjects(Set<Integer> projectIds, String searchValue, boolean archive, Sort sort) {
        return userRepository.findAllInProjectsByFirstNameOrLastNameAndArchive(projectIds, searchValue, archive, sort);
    }

    public void unArchiveUser(int userId) {
        getArchivedUser(userId)
            .orElseThrow(
                    () -> new ResourceNotFoundException("Archived user was not found by userId = " + userId + ".")
            );

        updateArchiveStatus(userId, false);
    }
}
