package ru.smartup.timetracker.repository.criteria;

import org.springframework.data.jpa.domain.Specification;
import ru.smartup.timetracker.entity.Project;
import ru.smartup.timetracker.repository.criteria.spec.ProjectSpecs;

import java.util.Set;

public class ProjectFilterBuilder {
    private Specification<Project> projectSpec;

    public ProjectFilterBuilder addIsArchiveFilter(final boolean archive) {
        if (projectSpec == null) {
            projectSpec = ProjectSpecs.filterByIsArchived(archive);
        } else {
            projectSpec = projectSpec.and(ProjectSpecs.filterByIsArchived(archive));
        }
        return this;
    }

    public ProjectFilterBuilder addNameFilter(final String searchValue) {
        if (projectSpec == null) {
            projectSpec = ProjectSpecs.filterByName(searchValue);
        } else {
            projectSpec = projectSpec.and(ProjectSpecs.filterByName(searchValue));
        }
        return this;
    }

    public ProjectFilterBuilder addIdsFilter(final Set<Integer> ids) {
        if (projectSpec == null) {
            projectSpec = ProjectSpecs.filterByIds(ids);
        } else {
            projectSpec = projectSpec.and(ProjectSpecs.filterByIds(ids));
        }
        return this;
    }

    public Specification<Project> buildSpecification() {
        return Specification.where(projectSpec);
    }
}
