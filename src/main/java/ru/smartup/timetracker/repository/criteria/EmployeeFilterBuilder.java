package ru.smartup.timetracker.repository.criteria;

import org.springframework.data.jpa.domain.Specification;
import ru.smartup.timetracker.entity.Employee;
import ru.smartup.timetracker.repository.criteria.spec.EmployeeSpecs;
import ru.smartup.timetracker.utils.CommonStringUtils;

public class EmployeeFilterBuilder {

    private Specification<Employee> employeeSpec;

    public EmployeeFilterBuilder() {
        this.employeeSpec = null;
    }

    public EmployeeFilterBuilder addIsArchiveFilter(final boolean archive) {
        if (employeeSpec == null) {
            employeeSpec = EmployeeSpecs.filterByIsArchived(archive);
        } else {
            employeeSpec = employeeSpec.and(EmployeeSpecs.filterByIsArchived(archive));
        }
        return this;
    }

    public EmployeeFilterBuilder addNameFilter(final String searchValue) {
        int numberOfParts = 2;
        String[] searchValueArr = searchValue.split(CommonStringUtils.WHITESPACE_REG_EXP, numberOfParts);
        Specification<Employee> tempSpec;
        if (searchValueArr.length == numberOfParts) {
            tempSpec = EmployeeSpecs.filterByFullName(searchValueArr[0], searchValueArr[1]);
        } else {
            tempSpec = EmployeeSpecs.filterByFirstOrLastName(searchValue);
        }

        if (employeeSpec == null) {
            employeeSpec = tempSpec;
        } else {
            employeeSpec = employeeSpec.and(tempSpec);
        }

        return this;
    }

    public Specification<Employee> buildSpecification() {
        return Specification.where(employeeSpec);
    }
}
