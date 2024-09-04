package ru.smartup.timetracker.repository.criteria.spec;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import ru.smartup.timetracker.entity.Position;

@UtilityClass
public class PositionSpecs {

    public static Specification<Position> filterByName(String searchValue) {
        return (root, query, builder) -> builder.like(builder.lower(root.get("name")),
                "%" + StringUtils.lowerCase(searchValue) + "%");
    }

    public static Specification<Position> filterByIsArchived(boolean isArchived) {
        return (root, query, builder) -> builder.equal(root.get("isArchived"), isArchived);
    }

}
