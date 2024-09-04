package ru.smartup.timetracker.repository.criteria;

import org.springframework.data.jpa.domain.Specification;
import ru.smartup.timetracker.entity.Position;
import ru.smartup.timetracker.repository.criteria.spec.PositionSpecs;

public class PositionFilterBuilder {
    private Specification<Position> positionSpec;

    public PositionFilterBuilder addIsArchiveFilter(final boolean archive) {
        if (positionSpec == null) {
            positionSpec = PositionSpecs.filterByIsArchived(archive);
        } else {
            positionSpec = positionSpec.and(PositionSpecs.filterByIsArchived(archive));
        }
        return this;
    }

    public PositionFilterBuilder addNameFilter(final String searchValue) {
        if (positionSpec == null) {
            positionSpec = PositionSpecs.filterByName(searchValue);
        } else {
            positionSpec = positionSpec.and(PositionSpecs.filterByName(searchValue));
        }
        return this;
    }

    public Specification<Position> buildSpecification() {
        return Specification.where(positionSpec);
    }
}
