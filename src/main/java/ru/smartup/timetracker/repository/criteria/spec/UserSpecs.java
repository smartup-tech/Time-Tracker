package ru.smartup.timetracker.repository.criteria.spec;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import ru.smartup.timetracker.entity.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@UtilityClass
public class UserSpecs {

    public static Specification<User> filterByFirstOrLastName(String searchValue) {
        return (root, query, builder) -> builder.or(containingIgnoreCase("firstName", searchValue, root, builder),
                containingIgnoreCase("lastName", searchValue, root, builder));
    }

    public static Specification<User> filterByFullName(String partOfFirstName, String partOfLastName) {
        return (root, query, builder) -> builder.and(
                builder.or(containingIgnoreCase("firstName", partOfFirstName, root, builder), containingIgnoreCase("lastName", partOfLastName, root, builder),
                builder.or(containingIgnoreCase("firstName", partOfLastName, root, builder), containingIgnoreCase("lastName", partOfFirstName, root, builder)
        )));
    }

    public static Specification<User> filterByIsArchived(boolean isArchived) {
        return (root, query, builder) -> builder.equal(root.get("isArchived"), isArchived);
    }

    private static Predicate containingIgnoreCase(String property, String searchValue, Root<User> root, CriteriaBuilder builder) {
        return builder.like(builder.lower(root.get(property)), "%" + StringUtils.lowerCase(searchValue) + "%");
    }

}
