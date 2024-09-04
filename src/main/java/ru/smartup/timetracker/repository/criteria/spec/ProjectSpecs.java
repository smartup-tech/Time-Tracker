package ru.smartup.timetracker.repository.criteria.spec;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import ru.smartup.timetracker.entity.Project;

import java.util.Set;

@UtilityClass
public class ProjectSpecs {

    public static Specification<Project> filterByName(String searchValue) {
        return (root, query, builder) -> builder.like(builder.lower(root.get("name")),
                "%" + StringUtils.lowerCase(searchValue) + "%");
    }

    public static Specification<Project> filterByIsArchived(boolean isArchived) {
        return (root, query, builder) -> builder.equal(root.get("isArchived"), isArchived);
    }

    public static Specification<Project> filterByIds(Set<Integer> projectIds) {
        return (root, query, builder) -> root.get("id").in(projectIds);
    }

}
