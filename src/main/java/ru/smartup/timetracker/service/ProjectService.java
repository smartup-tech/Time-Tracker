package ru.smartup.timetracker.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.smartup.timetracker.core.CacheNames;
import ru.smartup.timetracker.dto.PageableRequestParamDto;
import ru.smartup.timetracker.dto.QueryArchiveParamRequestDto;
import ru.smartup.timetracker.dto.project.response.ProjectShortDto;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.entity.field.sort.ProjectSortFieldEnum;
import ru.smartup.timetracker.pojo.ProjectOfUser;
import ru.smartup.timetracker.repository.ProjectRepository;
import ru.smartup.timetracker.repository.TaskRepository;
import ru.smartup.timetracker.repository.criteria.ProjectFilterBuilder;
import ru.smartup.timetracker.utils.PageableMaker;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PageableMaker pageableMaker;
    private final ModelMapper modelMapper;

    public Page<ProjectShortDto> getProjects(final QueryArchiveParamRequestDto queryArchiveParam, final PageableRequestParamDto<ProjectSortFieldEnum> pageableParam) {

        Page<Project> projects = getPageableAndFilteredProjects(queryArchiveParam, pageableParam);

        return projects.map(project -> modelMapper.map(project, ProjectShortDto.class));
    }

    private Page<Project> getPageableAndFilteredProjects(final QueryArchiveParamRequestDto queryArchiveParam, final PageableRequestParamDto<ProjectSortFieldEnum> pageableParam) {
        Pageable pageable = pageableMaker.make(pageableParam);
        Specification<Project> projectFilters = getProjectFilters(queryArchiveParam.getQuery(), queryArchiveParam.isArchive());
        return getProjects(projectFilters, pageable);
    }

    private Specification<Project> getProjectFilters(final String searchValue, final boolean archive) {
        ProjectFilterBuilder builder = new ProjectFilterBuilder();

        builder.addIsArchiveFilter(archive);

        if (!searchValue.isBlank()) {
            builder.addNameFilter(searchValue);
        }

        return builder.buildSpecification();
    }

    private Specification<Project> getProjectFilters(final Set<Integer> projectIds, final String searchValue, final boolean archive) {
        ProjectFilterBuilder builder = new ProjectFilterBuilder();

        builder.addIsArchiveFilter(archive);

        if (!searchValue.isBlank()) {
            builder.addNameFilter(searchValue);
        }

        if (projectIds != null && !projectIds.isEmpty()) {
            builder.addIdsFilter(projectIds);
        }

        return builder.buildSpecification();
    }

    @Cacheable(cacheNames = CacheNames.Project.GET_PROJECTS_SEARCH_PAGEABLE)
    public Page<Project> getProjects(Specification<Project> projectFilters, Pageable pageable) {
        return projectRepository.findAll(projectFilters, pageable);
    }

    @Cacheable(cacheNames = CacheNames.Project.GET_ALL_PROJECTS)
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Cacheable(cacheNames = CacheNames.Project.GET_ACTIVE_PROJECTS)
    public List<Project> getActiveProjects() {
        return projectRepository.findAllByIsArchivedFalseOrderByName();
    }

    @Cacheable(cacheNames = CacheNames.Project.GET_PROJECT_BY_ID)
    public Optional<Project> getProject(int projectId) {
        return projectRepository.findById(projectId);
    }

    @Cacheable(cacheNames = CacheNames.Project.GET_NOT_ARCHIVED_PROJECT_BY_ID)
    public Optional<Project> getNotArchivedProject(int projectId) {
        return projectRepository.findByIdAndIsArchivedFalse(projectId);
    }

    public Page<ProjectShortDto> getProjectsByIds(Set<Integer> projectIds, final QueryArchiveParamRequestDto queryArchiveParam, final PageableRequestParamDto<ProjectSortFieldEnum> pageableParam) {

        Pageable pageable = pageableMaker.make(pageableParam);
        Specification<Project> projectFilters = getProjectFilters(projectIds, queryArchiveParam.getQuery(), queryArchiveParam.isArchive());

        Page<Project> projects = projectRepository.findAll(projectFilters, pageable);
        return projects.map(project -> modelMapper.map(project, ProjectShortDto.class));
    }

    public List<Project> getProjectsByIds(Set<Integer> projectIds) {
        return projectRepository.findAllByIdIn(projectIds);
    }

    public List<Project> getNotArchivedProjectsOfUser(int userId) {
        return projectRepository.findAllNotArchivedProjectsOfUser(userId);
    }

    public List<ProjectOfUser> getNotArchivedProjectsOfUserWithRole(int userId) {
        return projectRepository.findAllNotArchivedProjectsOfUserWithRole(userId).stream()
                .map(projectWithRole -> new ProjectOfUser(
                        projectWithRole.getId(),
                        projectWithRole.getName(),
                        projectWithRole.isArchived(),
                        projectWithRole.getExternalRate(),
                        projectWithRole.getProjectRoleId()))
                .collect(Collectors.toList());
    }

    public boolean isNotUnique(String projectName) {
        return projectRepository.isNotUnique(projectName);
    }

    public boolean isNotUnique(int projectId, String projectName) {
        return projectRepository.isNotUnique(projectId, projectName);
    }

    /**
     * Создать проект
     *
     * @param project проект
     */
    @CacheEvict(value = {
            CacheNames.Project.GET_ACTIVE_PROJECTS,
            CacheNames.Project.GET_ALL_PROJECTS,
            CacheNames.Project.GET_PROJECTS_SEARCH_PAGEABLE,
            CacheNames.Project.GET_PROJECT_BY_ID,
            CacheNames.Project.GET_NOT_ARCHIVED_PROJECT_BY_ID},
            allEntries = true)
    @Transactional
    public void createProject(Project project) {
        projectRepository.save(project);
    }

    /**
     * Обновить проект
     *
     * @param project проект
     */
    @CacheEvict(value = {
            CacheNames.Project.GET_ACTIVE_PROJECTS,
            CacheNames.Project.GET_ALL_PROJECTS,
            CacheNames.Project.GET_PROJECTS_SEARCH_PAGEABLE,
            CacheNames.Project.GET_PROJECT_BY_ID,
            CacheNames.Project.GET_NOT_ARCHIVED_PROJECT_BY_ID},
            allEntries = true)
    @Transactional
    public void updateProject(Project project) {
        projectRepository.save(project);
    }

    /**
     * Переместить проект в архив
     *
     * @param projectId идентификатор проекта
     */
    @CacheEvict(value = {
            CacheNames.Project.GET_ACTIVE_PROJECTS,
            CacheNames.Project.GET_ALL_PROJECTS,
            CacheNames.Project.GET_PROJECTS_SEARCH_PAGEABLE,
            CacheNames.Project.GET_PROJECT_BY_ID,
            CacheNames.Project.GET_NOT_ARCHIVED_PROJECT_BY_ID},
            allEntries = true)
    @Transactional
    public void archiveProject(int projectId) {
        taskRepository.archiveAllTasksFromProject(projectId);
        projectRepository.archiveProject(projectId);
    }
}
