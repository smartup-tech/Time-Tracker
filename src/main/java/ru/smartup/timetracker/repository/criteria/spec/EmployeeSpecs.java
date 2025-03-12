package ru.smartup.timetracker.repository.criteria.spec;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import ru.smartup.timetracker.entity.Employee;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@UtilityClass
public class EmployeeSpecs {

    public static Specification<Employee> filterByFirstOrLastName(String searchValue) {
        return (root, query, builder) -> builder.or(containingIgnoreCase("firstName", searchValue, root, builder),
                containingIgnoreCase("lastName", searchValue, root, builder));
    }

    public static Specification<Employee> filterByFullName(String partOfFirstName, String partOfLastName) {
        return (root, query, builder) -> builder.and(
                builder.or(containingIgnoreCase("firstName", partOfFirstName, root, builder), containingIgnoreCase("lastName", partOfLastName, root, builder),
                builder.or(containingIgnoreCase("firstName", partOfLastName, root, builder), containingIgnoreCase("lastName", partOfFirstName, root, builder)
        )));
    }

    public static Specification<Employee> filterByIsArchived(boolean isArchived) {
        return (root, query, builder) -> builder.equal(root.get("isArchived"), isArchived);
    }

    private static Predicate containingIgnoreCase(String property, String searchValue, Root<Employee> root, CriteriaBuilder builder) {
        return builder.like(builder.lower(root.get(property)), "%" + StringUtils.lowerCase(searchValue) + "%");
    }

}
