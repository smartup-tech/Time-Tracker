package ru.smartup.timetracker.repository.criteria;

import org.springframework.data.jpa.domain.Specification;
import ru.smartup.timetracker.entity.User;
import ru.smartup.timetracker.repository.criteria.spec.UserSpecs;
import ru.smartup.timetracker.utils.CommonStringUtils;

public class UserFilterBuilder {

    private Specification<User> userSpec;

    public UserFilterBuilder() {
        this.userSpec = null;
    }

    public UserFilterBuilder addIsArchiveFilter(final boolean archive) {
        if (userSpec == null) {
            userSpec = UserSpecs.filterByIsArchived(archive);
        } else {
            userSpec = userSpec.and(UserSpecs.filterByIsArchived(archive));
        }
        return this;
    }

    public UserFilterBuilder addNameFilter(final String searchValue) {
        int numberOfParts = 2;
        String[] searchValueArr = searchValue.split(CommonStringUtils.WHITESPACE_REG_EXP, numberOfParts);
        Specification<User> tempSpec;
        if (searchValueArr.length == numberOfParts) {
            tempSpec = UserSpecs.filterByFullName(searchValueArr[0], searchValueArr[1]);
        } else {
            tempSpec = UserSpecs.filterByFirstOrLastName(searchValue);
        }

        if (userSpec == null) {
            userSpec = tempSpec;
        } else {
            userSpec = userSpec.and(tempSpec);
        }

        return this;
    }

    public Specification<User> buildSpecification() {
        return Specification.where(userSpec);
    }
}
