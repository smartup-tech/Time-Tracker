package ru.smartup.timetracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import ru.smartup.timetracker.core.SessionUserPrincipal;
import ru.smartup.timetracker.core.WebConfig;
import ru.smartup.timetracker.dto.approval.request.SubmittedHoursApproveDto;
import ru.smartup.timetracker.dto.approval.request.SubmittedHoursRejectDto;
import ru.smartup.timetracker.dto.approval.response.SubmittedHoursByProjectsDto;
import ru.smartup.timetracker.dto.approval.response.SubmittedHoursByWeekAndProjectDto;
import ru.smartup.timetracker.dto.approval.response.SubmittedHoursDto;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.enumerated.ProjectRoleEnum;
import ru.smartup.timetracker.entity.TrackUnit;
import ru.smartup.timetracker.entity.field.enumerated.TrackUnitStatusEnum;
import ru.smartup.timetracker.entity.UserProjectRole;
import ru.smartup.timetracker.entity.UserRole;
import ru.smartup.timetracker.entity.field.enumerated.UserRoleEnum;
import ru.smartup.timetracker.exception.ForbiddenException;
import ru.smartup.timetracker.exception.ResourceNotFoundException;
import ru.smartup.timetracker.pojo.SubmittedHours;
import ru.smartup.timetracker.pojo.SubmittedHoursByProjects;
import ru.smartup.timetracker.service.ProductionCalendarService;
import ru.smartup.timetracker.service.ProjectService;
import ru.smartup.timetracker.service.TrackUnitService;
import ru.smartup.timetracker.service.freeze.CRUDFreezeService;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ApprovalRestControllerTest {
    private static final int USER_ID = 1;
    private static final int PROJECT_ID_1 = 1;
    private static final int PROJECT_ID_2 = 2;
    private static final int PROJECT_ID_3 = 3;
    private static final float DAY_HOURS = 8;
    private static final float WEEK_HOURS = 40;
    private static final float TOTAL_HOURS = 100;
    private static final long TASK_ID = 1;
    private static final long TRACK_UNIT_ID_1 = 1;
    private static final long TRACK_UNIT_ID_2 = 2;
    private static final long TRACK_UNIT_ID_3 = 3;
    private static final String USER_FIRST_NAME = "first name";
    private static final String USER_LAST_NAME = "last name";
    private static final String USER_EMAIL = "email";
    private static final String PROJECT_NAME_1 = "project name 1";
    private static final String PROJECT_NAME_2 = "project name 2";
    private static final String TASK_NAME = "task name";
    private static final String REJECT_REASON = "reject reason";
    private static final LocalDate WEEK_1 = LocalDate.of(2022, 12, 12);
    private static final LocalDate WEEK_2 = LocalDate.of(2022, 12, 19);
    private final TrackUnitService trackUnitService = mock(TrackUnitService.class);
    private final ProjectService projectService = mock(ProjectService.class);
    private final ProductionCalendarService productionCalendarService = mock(ProductionCalendarService.class);
    private CRUDFreezeService CRUDFreezeService;
    private ApprovalRestController approvalRestController;

    @BeforeEach
    public void setUp() {
        ModelMapper modelMapper = new WebConfig().modelMapper();
        approvalRestController = new ApprovalRestController(trackUnitService, projectService, productionCalendarService, CRUDFreezeService, modelMapper);
    }

    @Test
    public void getSubmittedHours_whenAdmin() {
        List<SubmittedHours> submittedHours = List.of(new SubmittedHours(WEEK_1, WEEK_HOURS),
                new SubmittedHours(WEEK_2, WEEK_HOURS));
        when(trackUnitService.getSubmittedHours()).thenReturn(submittedHours);

        List<SubmittedHoursDto> hours = approvalRestController.getSubmittedHours(
                createSessionUserPrincipal(UserRoleEnum.ROLE_ADMIN));

        assertEquals(2, hours.size());
    }

    @Test
    public void getSubmittedHours_whenManager() {
        List<SubmittedHours> submittedHours = List.of(new SubmittedHours(WEEK_1, WEEK_HOURS),
                new SubmittedHours(WEEK_2, WEEK_HOURS));
        when(trackUnitService.getSubmittedHours(Set.of(PROJECT_ID_1, PROJECT_ID_2))).thenReturn(submittedHours);

        List<SubmittedHoursDto> hours = approvalRestController.getSubmittedHours(createSessionUserPrincipalForManager());

        assertEquals(2, hours.size());
    }

    @Test
    public void getSubmittedHours_whenUser_shouldReturnException() {
        assertThrows(ForbiddenException.class, () -> approvalRestController.getSubmittedHours(
                createSessionUserPrincipal(UserRoleEnum.ROLE_USER)));
        verify(trackUnitService, never()).getSubmittedHours(anySet());
    }

    @Test
    public void getSubmittedHoursByProjects_whenAdmin() {
        List<SubmittedHoursByProjects> submittedHoursByProjects = List.of(
                new SubmittedHoursByProjects(PROJECT_ID_1, PROJECT_NAME_1, WEEK_HOURS, TOTAL_HOURS),
                new SubmittedHoursByProjects(PROJECT_ID_2, PROJECT_NAME_2, WEEK_HOURS, TOTAL_HOURS)
        );
        when(trackUnitService.getSubmittedHoursByProjects(Date.valueOf(WEEK_1))).thenReturn(submittedHoursByProjects);

        List<SubmittedHoursByProjectsDto> hours = approvalRestController.getSubmittedHoursByProjects(
                createSessionUserPrincipal(UserRoleEnum.ROLE_ADMIN), WEEK_1);

        assertEquals(2, hours.size());
    }

    @Test
    public void getSubmittedHoursByProjects_whenManager() {
        List<SubmittedHoursByProjects> submittedHoursByProjects = List.of(
                new SubmittedHoursByProjects(PROJECT_ID_1, PROJECT_NAME_1, WEEK_HOURS, TOTAL_HOURS),
                new SubmittedHoursByProjects(PROJECT_ID_2, PROJECT_NAME_2, WEEK_HOURS, TOTAL_HOURS)
        );
        when(trackUnitService.getSubmittedHoursByProjects(Date.valueOf(WEEK_1), Set.of(PROJECT_ID_1, PROJECT_ID_2)))
                .thenReturn(submittedHoursByProjects);

        List<SubmittedHoursByProjectsDto> hours = approvalRestController.getSubmittedHoursByProjects(
                createSessionUserPrincipalForManager(), WEEK_1);

        assertEquals(2, hours.size());
    }

    @Test
    public void getSubmittedHoursByProjects_whenUser_shouldReturnException() {
        assertThrows(ForbiddenException.class, () -> approvalRestController.getSubmittedHoursByProjects(
                createSessionUserPrincipal(UserRoleEnum.ROLE_USER), WEEK_1));
        verify(trackUnitService, never()).getSubmittedHoursByProjects(eq(Date.valueOf(WEEK_1)), anySet());
    }

    @Test
    public void getSubmittedHoursForApproval() {
        when(projectService.getNotArchivedProject(PROJECT_ID_1)).thenReturn(createProject());
        when(trackUnitService.getSubmittedHoursByWeekAndProjectId(Date.valueOf(WEEK_1), PROJECT_ID_1))
                .thenReturn(createListOfTrackUnits());

        List<SubmittedHoursByWeekAndProjectDto> hours =
                approvalRestController.getSubmittedHoursForApproval(WEEK_1, PROJECT_ID_1);

        assertEquals(1, hours.size());
    }

    @Test
    public void getSubmittedHoursForApproval_shouldReturnException() {
        when(projectService.getNotArchivedProject(PROJECT_ID_1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                approvalRestController.getSubmittedHoursForApproval(WEEK_1, PROJECT_ID_1));
        verify(trackUnitService, never()).getSubmittedHoursByWeekAndProjectId(Date.valueOf(WEEK_1), PROJECT_ID_1);
    }

    @Test
    public void approveHours_whenAdmin() {
        List<Long> trackUnitIds = List.of(TRACK_UNIT_ID_1, TRACK_UNIT_ID_2, TRACK_UNIT_ID_3);
        SubmittedHoursApproveDto submittedHoursApproveDto = new SubmittedHoursApproveDto();
        submittedHoursApproveDto.setTrackUnitIds(trackUnitIds);

        approvalRestController.approveHours(createSessionUserPrincipal(UserRoleEnum.ROLE_ADMIN), submittedHoursApproveDto);

        verify(trackUnitService).approve(trackUnitIds);
    }

    @Test
    public void approveHours_whenManager() {
        List<Long> trackUnitIds = List.of(TRACK_UNIT_ID_1, TRACK_UNIT_ID_2, TRACK_UNIT_ID_3);
        SubmittedHoursApproveDto submittedHoursApproveDto = new SubmittedHoursApproveDto();
        submittedHoursApproveDto.setTrackUnitIds(trackUnitIds);
        when(trackUnitService.getProjectIdsForTrackUnits(trackUnitIds)).thenReturn(Set.of(PROJECT_ID_1));

        approvalRestController.approveHours(createSessionUserPrincipalForManager(), submittedHoursApproveDto);

        verify(trackUnitService).approve(trackUnitIds);
    }

    @Test
    public void approveHours_whenManagerAndEmptyProjectIds() {
        List<Long> trackUnitIds = List.of(TRACK_UNIT_ID_1, TRACK_UNIT_ID_2, TRACK_UNIT_ID_3);
        SubmittedHoursApproveDto submittedHoursApproveDto = new SubmittedHoursApproveDto();
        submittedHoursApproveDto.setTrackUnitIds(trackUnitIds);
        when(trackUnitService.getProjectIdsForTrackUnits(trackUnitIds)).thenReturn(Set.of());

        approvalRestController.approveHours(createSessionUserPrincipalForManager(), submittedHoursApproveDto);

        verify(trackUnitService, never()).approve(trackUnitIds);
    }

    @Test
    public void approveHours_shouldReturnException() {
        List<Long> trackUnitIds = List.of(TRACK_UNIT_ID_1, TRACK_UNIT_ID_2, TRACK_UNIT_ID_3);
        SubmittedHoursApproveDto submittedHoursApproveDto = new SubmittedHoursApproveDto();
        submittedHoursApproveDto.setTrackUnitIds(trackUnitIds);
        when(trackUnitService.getProjectIdsForTrackUnits(trackUnitIds)).thenReturn(Set.of(PROJECT_ID_3));

        assertThrows(ForbiddenException.class, () ->
                approvalRestController.approveHours(createSessionUserPrincipalForManager(), submittedHoursApproveDto));
        verify(trackUnitService, never()).approve(trackUnitIds);
    }

    @Test
    public void rejectHours_whenAdmin() {
        List<Long> trackUnitIds = List.of(TRACK_UNIT_ID_1, TRACK_UNIT_ID_2, TRACK_UNIT_ID_3);
        SubmittedHoursRejectDto submittedHoursRejectDto = new SubmittedHoursRejectDto();
        submittedHoursRejectDto.setTrackUnitIds(trackUnitIds);
        submittedHoursRejectDto.setRejectReason(REJECT_REASON);

        approvalRestController.rejectHours(createSessionUserPrincipal(UserRoleEnum.ROLE_ADMIN), submittedHoursRejectDto);

        verify(trackUnitService).reject(trackUnitIds, REJECT_REASON);
    }

    @Test
    public void rejectHours_whenManager() {
        List<Long> trackUnitIds = List.of(TRACK_UNIT_ID_1, TRACK_UNIT_ID_2, TRACK_UNIT_ID_3);
        SubmittedHoursRejectDto submittedHoursRejectDto = new SubmittedHoursRejectDto();
        submittedHoursRejectDto.setTrackUnitIds(trackUnitIds);
        submittedHoursRejectDto.setRejectReason(REJECT_REASON);
        when(trackUnitService.getProjectIdsForTrackUnits(trackUnitIds)).thenReturn(Set.of(PROJECT_ID_1));

        approvalRestController.rejectHours(createSessionUserPrincipalForManager(), submittedHoursRejectDto);

        verify(trackUnitService).reject(trackUnitIds, REJECT_REASON);
    }

    @Test
    public void rejectHours_whenManagerAndEmptyProjectIds() {
        List<Long> trackUnitIds = List.of(TRACK_UNIT_ID_1, TRACK_UNIT_ID_2, TRACK_UNIT_ID_3);
        SubmittedHoursRejectDto submittedHoursRejectDto = new SubmittedHoursRejectDto();
        submittedHoursRejectDto.setTrackUnitIds(trackUnitIds);
        submittedHoursRejectDto.setRejectReason(REJECT_REASON);
        when(trackUnitService.getProjectIdsForTrackUnits(trackUnitIds)).thenReturn(Set.of());

        approvalRestController.rejectHours(createSessionUserPrincipalForManager(), submittedHoursRejectDto);

        verify(trackUnitService, never()).reject(trackUnitIds, REJECT_REASON);
    }

    @Test
    public void rejectHours_shouldReturnException() {
        List<Long> trackUnitIds = List.of(TRACK_UNIT_ID_1, TRACK_UNIT_ID_2, TRACK_UNIT_ID_3);
        SubmittedHoursRejectDto submittedHoursRejectDto = new SubmittedHoursRejectDto();
        submittedHoursRejectDto.setTrackUnitIds(trackUnitIds);
        submittedHoursRejectDto.setRejectReason(REJECT_REASON);
        when(trackUnitService.getProjectIdsForTrackUnits(trackUnitIds)).thenReturn(Set.of(PROJECT_ID_3));

        assertThrows(ForbiddenException.class, () ->
                approvalRestController.rejectHours(createSessionUserPrincipalForManager(), submittedHoursRejectDto));
        verify(trackUnitService, never()).reject(trackUnitIds, REJECT_REASON);
    }

    private SessionUserPrincipal createSessionUserPrincipal(UserRoleEnum role) {
        SessionUserPrincipal sessionUserPrincipal = new SessionUserPrincipal(USER_ID, USER_EMAIL);
        UserRole userRole = new UserRole();
        userRole.setUserId(USER_ID);
        userRole.setRoleId(role);
        sessionUserPrincipal.setAllRoles(List.of(userRole), List.of());
        return sessionUserPrincipal;
    }

    private SessionUserPrincipal createSessionUserPrincipalForManager() {
        SessionUserPrincipal sessionUserPrincipal = new SessionUserPrincipal(USER_ID, USER_EMAIL);
        UserRole userRole = new UserRole();
        userRole.setUserId(USER_ID);
        userRole.setRoleId(UserRoleEnum.ROLE_USER);
        UserProjectRole userProjectRole1 = new UserProjectRole();
        userProjectRole1.setProjectId(PROJECT_ID_1);
        userProjectRole1.setUserId(USER_ID);
        userProjectRole1.setProjectRoleId(ProjectRoleEnum.MANAGER);
        UserProjectRole userProjectRole2 = new UserProjectRole();
        userProjectRole2.setProjectId(PROJECT_ID_2);
        userProjectRole2.setUserId(USER_ID);
        userProjectRole2.setProjectRoleId(ProjectRoleEnum.MANAGER);
        sessionUserPrincipal.setAllRoles(List.of(userRole), List.of(userProjectRole1, userProjectRole2));
        return sessionUserPrincipal;
    }

    private List<TrackUnit> createListOfTrackUnits() {
        TrackUnit trackUnit = new TrackUnit(TRACK_UNIT_ID_1, USER_ID, USER_FIRST_NAME, USER_LAST_NAME, TASK_ID, TASK_NAME,
                java.util.Date.from(Instant.now()), DAY_HOURS, TrackUnitStatusEnum.SUBMITTED, true, null);
        return List.of(trackUnit);
    }

    private Optional<Project> createProject() {
        Project project = new Project();
        project.setId(PROJECT_ID_1);
        project.setName(PROJECT_NAME_1);
        return Optional.of(project);
    }
}